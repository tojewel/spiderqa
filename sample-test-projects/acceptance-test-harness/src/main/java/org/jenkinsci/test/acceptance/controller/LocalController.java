package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Injector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.Expand;
import org.codehaus.plexus.util.StringUtils;
import org.jenkinsci.test.acceptance.log.LogListenable;
import org.jenkinsci.test.acceptance.log.LogListener;
import org.jenkinsci.utils.process.ProcessInputStream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.System.*;

import org.jenkinsci.utils.process.CommandBuilder;

/**
 * Abstract base class for those JenkinsController that runs the JVM locally on
 * the same box as the test harness
 *
 * @author Vivek Pandey
 */
public abstract class LocalController extends JenkinsController implements LogListenable {
    /**
     * jenkins.war. Subject under test.
     */
    @Inject @Named("jenkins.war")
    protected /*final*/ File war;

    /**
     * JENKINS_HOME directory for jenkins.war to be launched.
     */
    protected final File tempDir;

    protected ProcessInputStream process;

    protected JenkinsLogWatcher logWatcher;

    private final Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            process.getProcess().destroy();
        }
    };

    private final File logFile;

    @Inject @Named("form-element-path.hpi")
    private File formElementPathPlugin;

    /**
     * Partial implementation of {@link JenkinsControllerFactory} for subtypes.
     */
    public static abstract class LocalFactoryImpl implements JenkinsControllerFactory {
    }

    protected LocalController(Injector i) {
        super(i);
        try {
            tempDir = File.createTempFile("jenkins", "home", new File(WORKSPACE));
            tempDir.delete();
            tempDir.mkdirs();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a temp file",e);
        }

        this.logFile = new File(this.tempDir.getParentFile(), this.tempDir.getName()+".log");
    }

    @Override
    public void postConstruct(Injector injector) {
        super.postConstruct(injector);

        File pluginDir = new File(tempDir,"plugins");
        pluginDir.mkdirs();

        File givenPluginDir = null;
        for (String d : Arrays.asList(
                getenv("PLUGINS_DIR"),
                new File(war.getParentFile(), "plugins").getAbsolutePath(),
                WORKSPACE + "/plugins",
                "plugins")) {
            if (d == null) {
                continue;
            }
            givenPluginDir = new File(d);
            if (givenPluginDir.isDirectory()) {
                break;
            }
        }

        if (givenPluginDir != null && givenPluginDir.isDirectory()) {
            try {
                FileUtils.copyDirectory(givenPluginDir, pluginDir);
            } catch (IOException e) {
                String msg = String.format("Failed to copy plugins from %s to %s", givenPluginDir, pluginDir);
                throw new RuntimeException(msg, e);
            }
        }

        System.out.println("running with given plugins: " + Arrays.toString(pluginDir.list()));

        try {
            FileUtils.copyFile(formElementPathPlugin, new File(pluginDir, "path-element.hpi"));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to copy form path element file %s to plugin dir %s.",
                    formElementPathPlugin, pluginDir),e);
        }
    }

    @Override
    public void addLogListener(LogListener l) {
        logWatcher.addLogListener(l);
    }

    @Override
    public void removeLogListener(LogListener l) {
        logWatcher.removeLogListener(l);
    }

    /**
     * @deprecated
     *      Use {@link #getJenkinsHome()}, which explains the nature of the directory better.
     */
    @Deprecated
    public File getTempDir() {
        return tempDir;
    }

    public File getSlaveJarPath() {
        return new File(getJenkinsHome(),"war/WEB-INF/slave.jar");
    }


    public File getJenkinsHome(){
        return tempDir;
    }

    @Override
    public void populateJenkinsHome(byte[] _template, boolean clean) throws IOException {
        try {
            if (clean && tempDir.isDirectory()) {
                FileUtils.cleanDirectory(tempDir);
            }
            if (!tempDir.isDirectory() && ! tempDir.mkdirs()) {
                throw new IOException("Could not create directory: " + tempDir);
            }
            File template = File.createTempFile("template", ".dat");
            try {
                FileUtils.writeByteArrayToFile(template, _template);
                Expand expand = new Expand();
                expand.setSrc(template);
                expand.setOverwrite(true);
                expand.setDest(tempDir);
                expand.execute();
            } finally {
                template.delete();
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public File getJavaHome() {
        String javaHome = getenv("JENKINS_JAVA_HOME");
        File home = StringUtils.isBlank(javaHome) ? null : new File(javaHome);
        if (home != null && home.isDirectory()) {
            return home;
        }
        javaHome = getenv("JAVA_HOME");
        home = StringUtils.isBlank(javaHome) ? null : new File(javaHome);
        if (home != null && home.isDirectory()) {
            return home;
        }
        javaHome = System.getProperty("java.home");
        home = StringUtils.isBlank(javaHome) ? null : new File(javaHome);
        if (home != null && home.isDirectory()) {
            return home;
        }
        return null;
    }

    public abstract ProcessInputStream startProcess() throws IOException;

    @Override
    public void startNow() throws IOException{
        this.process = startProcess();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        logWatcher = new JenkinsLogWatcher(getLogId(),process,logFile);
        logWatcher.start();
        try {
            LOGGER.info("Waiting for Jenkins to become running in "+ this);
            this.logWatcher.waitTillReady();
            LOGGER.info("Jenkins is running in " + this);
        } catch (Exception e) {
            diagnoseFailedLoad(e);
        }
    }

    @Override
    public void stopNow() throws IOException{
        process.getProcess().destroy();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    @Override
    public void diagnose(Throwable cause) {
        try {
            cause.printStackTrace(out);
            if(getenv("INTERACTIVE") != null && getenv("INTERACTIVE").equals("true")){
                out.println("Commencing interactive debugging. Browser session was kept open.");
                out.println("Press return to proceed.");
                new BufferedReader(new InputStreamReader(System.in)).readLine();
            }else{
                out.println("It looks like the test failed/errored, so here's the console from Jenkins:");
                out.println("--------------------------------------------------------------------------");
                out.println(FileUtils.readFileToString(logFile));
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Override
    public void tearDown(){
        try {
            if (logger != null) {
                logger.close();
            }

            FileUtils.forceDelete(tempDir);
        } catch (IOException e) {
            //maybe process is shutting down, wait for a sec then try again
            try {
                Thread.sleep(1000);
                FileUtils.forceDelete(tempDir);
            } catch (InterruptedException | IOException e1) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * Common environment variables to put to {@link CommandBuilder} when launching Jenkins.
     */
    protected @Nonnull Map<String, String> commonLaunchEnv() {
        HashMap<String, String> env = new HashMap<>();
        env.put("JENKINS_HOME", getJenkinsHome().getAbsolutePath());
        File javaHome = getJavaHome();
        if (javaHome != null) {
            env.put("JAVA_HOME", javaHome.getAbsolutePath());
        }
        return env;
    }

    /**
     * Gives random available port in the given range.
     *
     * @param from if <=0 then default value 49152 is used
     * @param to   if <=0 then default value 65535 is used
     */
    protected int randomLocalPort(int from, int to){
        from = (from <=0) ? 49152 : from;
        to = (to <= 0) ? 65535 : to;


        while(true){
            int candidate = (int) ((Math.random() * (to-from)) + from);
            if(isFreePort(candidate)){
                return candidate;
            }
            System.out.println(String.format("Port %s is in use", candidate));
        }
    }

    protected int randomLocalPort(){
        return randomLocalPort(-1,-1);
    }

    private void diagnoseFailedLoad(Exception cause) {
        Process proc = process.getProcess();

        try {
            int val = proc.exitValue();
            new RuntimeException("Jenkins died loading. Exit code " + val, cause);
        } catch (IllegalThreadStateException _) {
            // Process alive
        }

        // Try to get stacktrace
        Class<?> clazz;
        Field pidField;
        try {
            clazz = Class.forName("java.lang.UNIXProcess");
            pidField = clazz.getDeclaredField("pid");
            pidField.setAccessible(true);
        } catch (Exception e) {
            LinkageError x = new LinkageError();
            x.initCause(e);
            throw x;
        }

        if (clazz.isAssignableFrom(proc.getClass())) {
            int pid;
            try {
                pid = (int) pidField.get(proc);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new AssertionError(e);
            }

            try {
                Process jstack = new ProcessBuilder("jstack", String.valueOf(pid)).start();
                if (jstack.waitFor() == 0) {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(jstack.getInputStream(), writer);
                    RuntimeException ex = new RuntimeException(
                            cause.getMessage() + "\n\n" + writer.toString()
                    );
                    ex.setStackTrace(cause.getStackTrace());
                    throw ex;
                }
            } catch (IOException | InterruptedException e) {
                throw new AssertionError(e);
            }
        }

        throw new Error(cause);
    }

    private boolean  isFreePort(int port){
        try {
            ServerSocket ss = new ServerSocket(port);
            ss.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Hostname to use when accessing Jenkins.
     *
     * Useful to override with public hostname/IP when external clients needs to talk back to Jenkins.
     *
     * @return "127.0.0.1" unless overridden via JENKINS_LOCAL_HOSTNAME env var.
     */
    protected String getSutHostName() {
        String name = System.getenv("JENKINS_LOCAL_HOSTNAME");
        if (name == null || name.isEmpty()) {
            name = "127.0.0.1";
        }
        return name;
    }

    private static final Logger LOGGER = Logger.getLogger(LocalController.class.getName());
}
