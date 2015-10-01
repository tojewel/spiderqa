//
// +-----------------------------------------------------+
// |              =========================              |
// |              !  W  A  R  N  I  N  G  !              |
// |              =========================              |
// |                                                     |
// | This file is  N O T   P A R T  of the jenkins       |
// | acceptance test harness project's source code!      |
// |                                                     |
// | This file is only used for testing purposes w.r.t   |
// | the task scanner plugin test.                       |
// |                                                     |
// +-----------------------------------------------------+
//


package org.jenkinsci.test.acceptance.controller;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;

import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerImage;
import org.jenkinsci.test.acceptance.docker.fixtures.WinstoneContainer;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import com.cloudbees.sdk.extensibility.Extension;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Runs jenkins.war inside docker so that it gets a different IP address even though it's run on the same host.
 *
 * For efficiency, the docker container gets the entire host file system bind-mounted on it,
 * and we ssh into that box and start jenkins.
 *
 * @author Kohsuke Kawaguchi
 */
public class TSRWinstoneDockerController extends LocalController {
    @Inject
    Docker docker;

    private Class<? extends WinstoneContainer> fixtureType = WinstoneContainer.class;
    private String dockerImage;

    private WinstoneContainer container;

    public WinstoneDockerController(File war) {
        super(war);
    }

    public void setFixture(Class<? extends WinstoneContainer> fixtureType) {
        this.fixtureType = fixtureType;
    }

    public void setDockerImage(String img) {
        this.dockerImage = img;
    }

    @Override
    public ProcessInputStream startProcess() throws IOException {
        try {
            // can't mount symlink very well, so we need to resolve it
            File war = this.war.getCanonicalFile();

            CommandBuilder opts = new CommandBuilder();
            opts.add("-v", getJenkinsHome()+":/work");
            opts.add("-v", war.getParent()+":/war");

            // TODO: unify ID and fixture
            DockerImage img;
            if (dockerImage!=null)
                img = new DockerImage(dockerImage);
            else
                img = docker.build(fixtureType);

            container = img.start(fixtureType, opts, null);

            CommandBuilder cmds = new CommandBuilder();
            cmds.add("java");
            cmds.add("-DJENKINS_HOME=/work");
            cmds.add("-jar", "/war/" + war.getName());
            cmds.add("--ajp13Port=-1","--controlPort=8081","--httpPort=8080");
            return container.popen(cmds);
        } catch (InterruptedException e) {
            throw (IOException)new InterruptedIOException("Failed to launch winstone").initCause(e);
        }
    }

    @Override
    public void stopNow() throws IOException {
        try {
            super.stopNow();
        } finally {
            if (container!=null)
                container.close();
        }
    }

    public WinstoneContainer getContainer() {
        return container;
    }

    @Override
    public URL getUrl() {
        try {
            return new URL("http://"+container.getIpAddress()+":8080/");
        } catch (IOException e) {
            throw new AssertionError(String.format("%s failed to report its IP address",container),e);
        }
    }

    @Extension
    public static class FactoryImpl extends LocalFactoryImpl {
        @Inject
        Injector injector;

        @Override
        public String getId() {
            return "winstone_docker";
        }

        @Override
        public WinstoneDockerController create() {
            WinstoneDockerController c = new WinstoneDockerController(getWarFile());
            injector.injectMembers(c);
            String img = System.getenv("DOCKER_IMAGE");
            if (img!=null)
                c.setDockerImage(img);
            return c;
        }
    }
}

