package org.jenkinsci.test.acceptance.machine;

import com.google.inject.Inject;
import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.RemoteJenkinsController;
import org.jenkinsci.test.acceptance.Ssh;
import org.jenkinsci.test.acceptance.SshKeyPair;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.resolver.JenkinsResolver;
import org.jenkinsci.test.acceptance.resolver.PluginDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Creates {@link org.jenkinsci.test.acceptance.controller.JenkinsController} that launches Jenkins on a {@link org.jenkinsci.test.acceptance.machine.Machine}.
 *
 * @author Vivek Pandey
 */
@TestScope
public class RemoteJenkinsProvider extends JenkinsProvider {

    private static final Logger logger = LoggerFactory.getLogger(RemoteJenkinsProvider.class);

    private final MachineProvider machineProvider;

    private final JenkinsResolver jenkinsResolver;

    private final String jenkinsWar;

    private final File privateKeyFile;

    @Inject
    private TestCleaner cleaner;

    @Inject
    private Injector injector;

    @Inject
    public RemoteJenkinsProvider(MachineProvider provider, JenkinsResolver jenkinsResolver, SshKeyPair keyPair) {
        this.machineProvider = provider;
        this.jenkinsResolver = jenkinsResolver;
        this.privateKeyFile = keyPair.privateKey;
        logger.info("New Jenkins Provider created");
        this.jenkinsWar = JenkinsResolver.JENKINS_TEMP_DIR+"jenkins.war";
    }

    @Override
    public JenkinsController get() {
        logger.info("Creating new RemoteJenkinsController...");
        JenkinsController jenkinsController = createNewJenkinsController();
        try {
            cleaner.addTask(jenkinsController);
            jenkinsController.start();
        } catch (IOException e) {
            throw new AssertionError("Failed to start Jenkins: "+e.getMessage(),e);
        }
        return jenkinsController;
    }

    private JenkinsController createNewJenkinsController(){
        Machine machine = machineProvider.get();
        try{
            //install jenkins WAR
            jenkinsResolver.materialize(machine, jenkinsWar);
        }catch(Exception e){
            logger.error("Error during setting up Jenkins: "+e.getMessage(),e);
            throw new AssertionError(e);
        }

        String jenkinsHome = machine.dir()+newJenkinsHome()+"/";
        String pluginDir = jenkinsHome +"plugins/";
        String path = JenkinsResolver.JENKINS_TEMP_DIR+"form-element-path.hpi";

        //install form-path-element plugin
        new PluginDownloader("form-element-path").materialize(machine, path);
        try (Ssh ssh = machine.connect()) {
            ssh.executeRemoteCommand("mkdir -p " + pluginDir);

            ssh.executeRemoteCommand(String.format("cp %s %s", path, pluginDir));
        }
        return new RemoteJenkinsController(injector, machine, jenkinsHome,jenkinsWar,privateKeyFile);
    }

    private String newJenkinsHome(){
        return String.format("jenkins_home_%s", JcloudsMachine.newDirSuffix());
    }

}
