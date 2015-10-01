package plugins;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.FtpdContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.IPasswordDockerContainer;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.publish_over.*;
import org.jenkinsci.test.acceptance.plugins.publish_over.FtpGlobalConfig.FtpSite;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

/**
 * Feature: Tests for FTP plugin
 * Warning for a working Test of FTP with docker, we need to disable
 * RemoteVerification in the FTPClient of Java!
 *
 * @author Tobias Meyer
 */
@WithPlugins("publish-over-ftp")
public class FtpPublishPluginTest extends GlobalPublishPluginTest<FtpdContainer> {

    @Override
    protected PublishGlobalConfig.GlobalSite createGlobalConfig() {
        return new FtpGlobalConfig(jenkins).addSite();
    }

    @Override
    protected PublishGlobalPublisher addGlobalPublisher(FreeStyleJob j) {
        return j.addPublisher(FtpPublisher.class);
    }

    /**
     * Helper method to configure Jenkins.
     * It adds the DockerContainer as FTP Server with the name
     *
     * @param serverName Name to Access Instance
     * @param dock       Docker Instance of the Server
     */
    @Override
    protected void configurePublisher(String serverName, DockerContainer dock) {
        jenkins.configure();
        FtpSite s = new FtpGlobalConfig(jenkins).addSite();
        {
            s.name.set(serverName);
            s.hostname.set(dock.ipBound(21));
            s.port.set(dock.port(21));
            if(dock instanceof IPasswordDockerContainer) {
                s.username.set(((IPasswordDockerContainer)dock).getUsername());
                s.password.set(((IPasswordDockerContainer)dock).getPassword());
            }
            s.remoteDir.set("/tmp");
            s.disableRemoteVerification.check(true);
        }
        jenkins.save();
    }
}
