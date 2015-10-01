# Choosing the JenkinsController
This test harness has an abstraction called [JenkinsController](../src/main/java/org/jenkinsci/test/acceptance/controller/JenkinsController.java)
that allows you to use different logic for starting/stopping Jenkins.
We use this to so that the same set of tests can be run against many different ways of launching Jenkins,
such as `java -jar jenkins.war`, Jenkins on JBoss, Jenkins via debian package, etc.

To select a controller, run the test with the `TYPE` environment variable set to the controller ID, such as:
`TYPE=remote_sysv mvn test`.

Common configuration of Controllers can be done through environment variables, and the following controller-specific
section describes them.

For more sophisticated customization, see [WIRING.md](WIRING.md).

## Local family controllers
All local controllers run both test harness and Jenkins under test on local machine. Common environment variables for local controllers:

* `JENKINS_WAR` the path to `jenkins.war` to be tested. If not specified, the first of `WORKSPACE/jenkins.war` followed
    by `$(pwd)/jenkins.war` that is a file will be selected.
* `JENKINS_VERSION` specifies Jenkins core version to use. This is an alternative to `JENKINS_WAR`.
* `JENKINS_JAVA_HOME` the JVM home to use for running Jenkins. If not specified, the first of `JAVA_HOME`, or the JVM
   used to launch the tests will be used.
* `JENKINS_JAVA_OPTS` Adds additional options to the java process like `-Xms=XXm -Xmx=XXXm`.
* `PLUGINS_DIR` a directory of plugins to be loaded on Jenkins startup. If this is not specified, the first existing
    directory from the following list will be used: a `plugins` directory as a sibling to the resolved `jenkins.war`,
    `WORKSPACE/plugins` and `$(pwd)/plugins`. If the environment variable `NEVER_REPLACE_EXISTING_PLUGINS` is set
    then plugins will never be overwritten with newer versions during test.
* `INTERACTIVE` keep browser session opened after failed scenario for interactive investigation.

### Winstone controller (TYPE=winstone)
This controller runs Jenkins via `java -jar jenkins.war` on the same host where the test is run. This is the default controller.

### Winstone Docker controller (TYPE=winstone_docker)
This controller runs Jenkins via `java -jar jenkins.war` much like the Winstone controller, except
that it launches Winstone inside a docker container. This allows users to better control the environment
in which Jenkins runs in, without incurring the overhead of Vagrant.

* `DOCKER_IMAGE` the docker container image name (or image ID) to run Jenkins in.

The ability to specify the docker image is particularly useful to ensure that tests are run against
the consistent version of JDK and build tools.

### Tomcat controller (TYPE=tomcat)
This controller deploys Jenkins inside Tomcat and run the test with it. This controller requires a functioning Tomcat installation listening on port 8080, on the same system that the tests run. During the test, Jenkins is deployed here, and Tomcat gets started/stopped.

The behaviour of this controller can be customized through the following environment variables.

* `CATALINA_HOME` The directory in which Tomcat is already installed. If this is not specified, the first existing
    directory from the following list will be used: a `tomcat` directory as a sibling to the resolved `jenkins.war`,
    `WORKSPACE/tomcat` and `$(pwd)/tomcat`.

### JBoss controller (TYPE=jboss)
Similar to the above Tomcat controller except it uses JBoss.

The behaviour of this controller can be customized through the following environment variables.

* `JBOSS_HOME` The directory in which JBoss is already installed.  If this is not specified, the first existing
    directory from the following list will be used: a `jboss` directory as a sibling to the resolved `jenkins.war`,
    `WORKSPACE/jboss` and `$(pwd)/jboss`.

## 'Existing Jenkins' controller (TYPE=existing)
This controller assumes that you already have a Jenkins instance somewhere that you want the tests to run in.
Note that tests mutate this Jenkins instance, so this is not meant to be used with your production Jenkins instance.
Instead, run `java -jar jenkins.war` somewhere, and use this controller.

The behaviour of this controller can be customized through the following environment variables.

* `JENKINS_URL` the URL to the running Jenkins. Defaults to `http://localhost:8080/`

This controller is useful when you want to debug Jenkins while you run a test. It can be also used during
iterative test development to execute tests quickly.

## Vagrant family of controllers
There's a family of controllers who uses Vagrant to launch a virtual machine, then launch jenkins.war inside.

When run for the first time, this test harness will create a virtual machine.
To make repeated tests fast, the VM won't get shut down automatically at the end of a run, so the VM will keep running.

To do shut it down, cd `vagrant/*` and run `vagrant halt`. You can run any other vagrant commands
in this manner, which is useful for debugging.

You can also create `pre-configure.sh` and/or `post-configure.sh` in the current directory as needed
to customize how the Vagrant VM is initialized. These scripts are copied into the VM and then executed:

* `pre-configure.sh` runs before the controller attempts to install Jenkins
* `post-configure.sh` runs after the controller finished installing Jenkins

### Ubuntu controller (TYPE=ubuntu)
This controller uses Vagrant to run Ubuntu, then deploy Jenkins from an APT repository as a debian package. (This controller is not yet capable of testing individual `*.deb` file.)

* `REPO_URL` The location of APT repository in the format of `/etc/apt/sources.list`, such as `http://pkg.jenkins-ci.org/debian binary/`

### CentOS controller (TYPE=centos)
This controller uses Vagrant to run CentOS, then deploy Jenkins from an RPM repository.
This controller is not yet capable of testing individual `*.rpm` file.

* `REPO_URL` The location of RPM repository, such as `http://pkg.jenkins-ci.org/opensuse/`

### OpenSUSE controller (TYPE=opensuse)
This controller uses Vagrant to run CentOS, then deploy Jenkins from an RPM repository.
This controller is not yet capable of testing individual `*.rpm` file.

* `REPO_URL` The location of RPM repository, such as `http://pkg.jenkins-ci.org/opensuse/`
