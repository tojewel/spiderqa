/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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

import java.util.List;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jvnet.hudson.test.Issue;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.job_config_history.JobConfigHistory;
import org.jenkinsci.test.acceptance.plugins.job_config_history.JobConfigHistory.Change;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@WithPlugins("jobConfigHistory")
public class JobConfigHistoryPluginTest extends AbstractJUnitTest {

    @Test
    public void show_simple_history() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addShellStep("ls");
        job.save();

        job.action(JobConfigHistory.class).open();
        assertThat(driver, Matchers.hasContent("Created"));
        assertThat(all(by.xpath("//tr//a[contains(text(),'View as XML')]")).size(), is(greaterThan(2)));
        assertThat(all(by.xpath("//tr//a[contains(text(),'(RAW)')]")).size(), is(greaterThan(2)));
    }

    @Test @Issue("JENKINS-24410")
    public void track_change_in_matrix_project() {
        MatrixProject job = jenkins.jobs.create(MatrixProject.class);

        JobConfigHistory history = job.action(JobConfigHistory.class);

        List<Change> original = history.getChanges();

        job.configure();
        job.runSequentially.check();
        job.save();

        List<Change> newOnes = history.getChanges();

        assertThat("New changes saved", newOnes.size(), greaterThan(original.size()));
    }

    @Test @Issue("JENKINS-24410")
    public void track_change_in_maven_project() {
        MavenModuleSet job = jenkins.jobs.create(MavenModuleSet.class);

        JobConfigHistory history = job.action(JobConfigHistory.class);

        List<Change> original = history.getChanges();

        job.configure();
        job.goals.set("clean");
        job.save();

        List<Change> newOnes = history.getChanges();

        assertThat("New changes saved", newOnes.size(), greaterThan(original.size()));
    }

    @Test
    public void show_difference_in_config_history() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        ShellBuildStep step = job.addShellStep("ls");
        job.save();
        job.configure();
        step.command("ls -lh");
        job.save();

        JobConfigHistory action = job.action(JobConfigHistory.class);
        action.open();
        assertThat(driver, Matchers.hasContent("Created"));
        assertThat(driver, Matchers.hasContent("Changed"));

        action.showLastChange();
        assertThat(driver, Matchers.hasElement(by.xpath("//td[@class='diff_original']/pre[normalize-space(text())='#{original}']")));
        assertThat(driver, Matchers.hasElement(by.xpath("//td[@class='diff_revised']/pre[normalize-space(text())='#{current}']")));
    }
}
