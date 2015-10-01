# Writing JUnit tests

Test classes should be located to either `core` or `plugins` package.

`JenkinsAcceptanceTestRule` defines a [JUnit rule](https://github.com/junit-team/junit/wiki/Rules) that sets
up a test environment by instantiating a specified [JenkinsController](CONTROLLER.md), [WebDriver](BROWSER.md),
and other peripheral service components.

The run then perform Guice injection on the JUnit test instance, allowing you to access those components.
So a minimal test could look something like this:

    class HelloWorldTest {
        @Rule
        public JenkinsAcceptanceTestRule env = new JenkinsAcceptanceTestRule();

        // page object for Jenkins top page
        @Inject
        Jenkins jenkins;

        @Inject
        WebDriver driver;

        @Test
        public void hello() {
            // just check that the newly launched Jenkins is accessible
            jenkins.open();
        }
    }

[AbstractJUnitTest](../src/main/java/org/jenkinsci/test/acceptance/junit/AbstractJUnitTest.java) is a convenient
base class that pulls in various useful pieces by extending/implementing other types and defining helper methods.

## Marking tests for plugin dependencies
If your tests depend on specific plugins, put `@WithPlugins` annotation on your test method or class
to indicate that dependency.

This is preferable over installing plugins via UpdateCenter/PluginManager page objects, because it'll
allow filtering of tests based on plugins.

## Marking tests for credential dependencies
If your tests depend on specific credentials being present in the credentials plugin, put `@WithCredentials` annotation on your test method or class
to indicate that dependency.

Currently the annotation supports two different types of credentials:

username/password:
`@WithCredentials(credentialType = WithCredentials.USERNAME_PASSWORD, values = {"username", "password"})`

username/sshKey:
`@WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"username", "/ssh_keys/unsafe"})`



## Marking tests to be member of the smoke test group

Since the overall test suite runs a couple of hours you can use the predefined
set of "Smoke Tests" to get a first impression if everything is still running as expected.
You can run the smoke tests with `mvn -DrunSmokeTests`. If you want to add a test to the set of smoke tests
annotate the test method with the category `@Category(SmokeTest.class)`. Please make sure that the overall number
of smoke tests is small, e.g. not more than 10 tests.

## Testing recent features

Acceptance harness is designed to work against any version of Jenkins and its plugins.
Testing for recently added feature causes false negatives when older version is used
to run tests. To avoid that there is a way around this: use `@Since("X.Y")` to
declare a version of core or `@WithPlugins("plugin@X.Y")` for plugin version.

In code, one can use `Jenkins#getVersion()` or `Plugin.getVersion()` to implement
version agnostic page objects.

## Marking tests for immutablility
TODO
