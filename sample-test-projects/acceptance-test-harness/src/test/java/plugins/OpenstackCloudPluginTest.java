/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.TestActivation;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.config_file_provider.ConfigFileProvider;
import org.jenkinsci.test.acceptance.plugins.openstack.OpenstackCloud;
import org.jenkinsci.test.acceptance.plugins.openstack.OpenstackSlaveTemplate;
import org.jenkinsci.test.acceptance.plugins.openstack.UserDataConfig;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.MatrixBuild;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.jenkinsci.test.acceptance.po.MatrixRun;
import org.jenkinsci.test.acceptance.po.Node;
import org.junit.After;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import com.google.inject.Inject;

@WithPlugins("openstack-cloud")
@TestActivation({"ENDPOINT", "IDENTITY", "CREDENTIAL"})
public class OpenstackCloudPluginTest extends AbstractJUnitTest {

    private static final String CLOUD_INIT_NAME = "cloudInit";

    @Inject(optional = true) @Named("OpenstackCloudPluginTest.ENDPOINT")
    public String ENDPOINT;

    @Inject(optional = true) @Named("OpenstackCloudPluginTest.IDENTITY")
    public String IDENTITY;

    @Inject(optional = true) @Named("OpenstackCloudPluginTest.CREDENTIAL")
    public String CREDENTIAL;

    @Inject(optional = true) @Named("OpenstackCloudPluginTest.HARDWARE_ID")
    public String HARDWARE_ID;

    @Inject(optional = true) @Named("OpenstackCloudPluginTest.IMAGE_ID")
    public String IMAGE_ID;

    @Inject(optional = true) @Named("OpenstackCloudPluginTest.KEY_PAIR_NAME")
    public String KEY_PAIR_NAME;

    private final List<Node> nodes = new ArrayList<Node>();

    @After // Terminate all nodes
    public void tearDown() {
        for (Node n: nodes) {
            jenkins.runScript("Jenkins.instance.getNode('%s').tarminate()", n.getName());
        }
    }

    @Test
    public void testConnection() {
        jenkins.configure();
        OpenstackCloud cloud = addCloud(jenkins.getConfigPage());
        cloud.testConnection();
        waitFor(driver, hasContent("Connection succeeded!"), 60);
    }

    @Test
    @WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"root", "/openstack_plugin/unsafe"})
    @TestActivation({"HARDWARE_ID", "IMAGE_ID", "KEY_PAIR_NAME"})
    public void provisionSshSlave() {
        configureCloudInit("cloud-init");
        configureProvisioning("SSH", "label");

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.setLabelExpression("label");
        job.save();
        Build build = job.scheduleBuild().waitUntilFinished(240).shouldSucceed();

        nodes.add(build.getNode());
    }

    // The test will fail when test host is not reachable from openstack machine for obvious reasons
    @Test // @WithPlugins("openstack-cloud@1.9") JENKINS-29996
    @WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"root", "/openstack_plugin/unsafe"})
    @TestActivation({"HARDWARE_ID", "IMAGE_ID", "KEY_PAIR_NAME"})
    public void provisionJnlpSlave() {
        configureCloudInit("cloud-init-jnlp");
        configureProvisioning("JNLP", "label");

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.setLabelExpression("label");
        job.save();
        Build build = job.scheduleBuild().waitUntilFinished(240).shouldSucceed();

        nodes.add(build.getNode());
    }

    @Test @Issue("JENKINS-29998")
    @WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"root", "/openstack_plugin/unsafe"})
    @TestActivation({"HARDWARE_ID", "IMAGE_ID", "KEY_PAIR_NAME"})
    public void scheduleMatrixWithoutLabel() {
        configureCloudInit("cloud-init-jnlp");
        configureProvisioning("JNLP", "label");
        jenkins.configure();
        jenkins.getConfigPage().numExecutors.set(0);
        jenkins.save();

        MatrixProject job = jenkins.jobs.create(MatrixProject.class);
        job.configure();
        job.save();

        MatrixBuild pb = job.scheduleBuild().waitUntilFinished(240).shouldSucceed().as(MatrixBuild.class);
        assertThat(pb.getNode(), equalTo((Node) jenkins));
        MatrixRun cb = pb.getConfiguration("default");
        assertThat(cb.getNode(), not(equalTo((Node) jenkins)));

        nodes.add(cb.getNode());
    }

    private OpenstackCloud addCloud(JenkinsConfig config) {
        return config.addCloud(OpenstackCloud.class)
                .profile(Jenkins.createRandomName())
                .endpoint(ENDPOINT)
                .identity(IDENTITY)
                .credential(CREDENTIAL)
        ;
    }

    private void configureCloudInit(String cloudInitName) {
        ConfigFileProvider fileProvider = new ConfigFileProvider(jenkins);
        UserDataConfig cloudInit = fileProvider.addFile(UserDataConfig.class);
        cloudInit.open();
        cloudInit.name(CLOUD_INIT_NAME);
        cloudInit.content(resource("/openstack_plugin/" + cloudInitName).asText());
        cloudInit.save();
    }

    private void configureProvisioning(String type, String labels) {
        jenkins.configure();
        OpenstackCloud cloud = addCloud(jenkins.getConfigPage()).associateFloatingIp();
        OpenstackSlaveTemplate template = cloud.addSlaveTemplate();

        template.name("ath-integration-test");
        template.labels(labels);
        template.hardwareId(HARDWARE_ID);
        template.imageId(IMAGE_ID);
        template.credentials("root");
        template.slaveType(type);
        template.userData(CLOUD_INIT_NAME);
        template.keyPair(KEY_PAIR_NAME);
        jenkins.save();
    }
}
