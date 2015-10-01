package plugins;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.ArtifactoryContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.artifactory.ArtifactoryGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.artifactory.ArtifactoryGlobalConfig.Server;
import org.jenkinsci.test.acceptance.plugins.artifactory.ArtifactoryPublisher;
import org.jenkinsci.test.acceptance.plugins.artifactory.ArtifactoryGradleConfiguratior;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleInstallation;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleStep;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.junit.Test;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation.installSomeMaven;

/**
 * Checks the successfully integration of Artifactory plugin.
 */
@WithPlugins("artifactory")
@WithDocker
public class ArtifactoryPluginTest extends AbstractJUnitTest {

    @Inject
    DockerContainerHolder<ArtifactoryContainer> artifactoryContainer;

    @Test
    public void check_config_is_persisted() {
        final ArtifactoryContainer artifactory = artifactoryContainer.get();
        waitForArtifactory(artifactory);
        ArtifactoryGlobalConfig.Server server = configureArtifactory(artifactory);

        jenkins.configure();

        server.testConnectionButton.click();
        waitFor(driver, hasContent("Found Artifactory"), 10);

        server.username.set("bob");
        server.testConnectionButton.click();
        waitFor(driver, hasContent("Error occurred while requesting version information: Unauthorized"), 10);

        server.url.set("http://localhost:4898/blabla");
        server.testConnectionButton.click();
        waitFor(driver, hasContent("Connection to http://localhost:4898 refused"), 10);
    }

    @Test @WithPlugins("maven-plugin")
    public void maven_integration() {
        installSomeMaven(jenkins);
        final ArtifactoryContainer artifactory = artifactoryContainer.get();
        waitForArtifactory(artifactory);
        configureArtifactory(artifactory);

        MavenModuleSet job = jenkins.jobs.create(MavenModuleSet.class);
        job.configure();
        job.copyDir(resource("/artifactory_plugin/multimodule/"));
        job.goals.set("install");
        job.options("-verbose");
        ArtifactoryPublisher publisher = job.addPublisher(ArtifactoryPublisher.class);
        publisher.refresh();
        job.save();

        Build build = job.startBuild().shouldSucceed();

        String log = build.getConsole();
        assertThat(log, containsString("Initializing Artifactory Build-Info Recording"));
        assertThat(log, containsString("Deploying artifact"));
        assertThat(log, containsRegexp("Deploying build (info|descriptor) to: " + artifactory.getURL() + "/api/build"));
    }

    @Test @WithPlugins("gradle")
    public void gradle_integration() {
        final ArtifactoryContainer artifactory = artifactoryContainer.get();
        waitForArtifactory(artifactory);
        configureArtifactory(artifactory);

        GradleInstallation.installGradle(jenkins, "gradle 1.12", "1.12");

        FreeStyleJob job = jenkins.jobs.create();
        job.copyDir(resource("/artifactory_plugin/quickstart"));
        ArtifactoryGradleConfiguratior gradleConfig = new ArtifactoryGradleConfiguratior(job);
        gradleConfig.refresh();
        GradleStep gradle = job.addBuildStep(GradleStep.class);
        gradle.name.select("gradle 1.12");
        gradle.tasks.set("build");
        job.save();

        Build build = job.startBuild().shouldSucceed();

        String log = build.getConsole();
        assertThat(log, containsString("build artifactoryPublish"));
        assertThat(log, containsString("[buildinfo]"));
        assertThat(log, containsString("Deploying artifact"));
        assertThat(log, containsRegexp("Deploying build (info|descriptor) to: " + artifactory.getURL() + "/api/build"));
    }

    ArtifactoryGlobalConfig.Server configureArtifactory(ArtifactoryContainer artifactory) {
        JenkinsConfig config = jenkins.getConfigPage();
        config.configure();
        ArtifactoryGlobalConfig global = new ArtifactoryGlobalConfig(config);
        Server server = global.addServer();
        server.url.set(artifactory.getURL());
        server.username.set("admin");
        server.password.set("password");
        config.save();
        return server;
    }

    void waitForArtifactory(final ArtifactoryContainer artifactory) {
        jenkins.waitFor().withMessage("Artifactory is up").until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    String s = IOUtils.toString(artifactory.getPingURL().openStream());
                    return s.contains("OK");
                } catch (IOException e) {//catching IOException when server in not fully up and retuning 503
                    return null;
                }
            }
        });
    }
}
