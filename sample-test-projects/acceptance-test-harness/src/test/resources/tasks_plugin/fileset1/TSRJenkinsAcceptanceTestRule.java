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


package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.guice.World;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Runs Guice container that houses {@link JenkinsController}, {@link WebDriver}, and so on.
 *
 * <p>
 * Add this rule to your Unit test class if you want to leverage this harness.
 *
 * <p>
 * This is the glue that connects JUnit to the logic of the test harness (but to support other test harnesses
 * like cucumber, we are trying to minimize what to put in here.)
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class TSRJenkinsAcceptanceTestRule implements MethodRule { // TODO should use TestRule instead
    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        final Description description = Description.createTestDescription(method.getMethod().getDeclaringClass(), method.getName(), method.getAnnotations());
        return new Statement() {
            @Inject JenkinsController controller;
            @Inject Injector injector;

            @Override
            public void evaluate() throws Throwable {
                World world = World.get();
                Injector injector = world.getInjector();

                world.startTestScope(description.getDisplayName());

                injector.injectMembers(target);
                injector.injectMembers(this);

                System.out.println("=== Starting " + description.getDisplayName());
                try {
                    decorateWithRules(base).evaluate();
                } catch (AssumptionViolatedException e) {
                    throw e;
                } catch (Exception|AssertionError e) { // Errors and failures
                    controller.diagnose(e);
                    throw e;
                } finally {
                    world.endTestScope();
                }
            }

            /**
             * Look for annotations on a test and honor {@link RuleAnnotation}s in them.
             */
            private Statement decorateWithRules(Statement body) {
                Set<Class<? extends Annotation>> annotations = new HashSet<>();
                collectAnnotationTypes(method.getMethod(), annotations);
                collectAnnotationTypes(target.getClass(), annotations);

                Description testDescription = Description.createTestDescription(target.getClass(), method.getName(), method.getAnnotations());
                for (Class<? extends  Annotation> a : annotations) {
                    RuleAnnotation r = a.getAnnotation(RuleAnnotation.class);
                    if (r!=null) {
                        TestRule tr = injector.getInstance(r.value());
                        body = tr.apply(body,testDescription);
                    }
                }
                return body;
            }

            private void collectAnnotationTypes(AnnotatedElement e, Collection<Class<? extends Annotation>> types) {
                for (Annotation a : e.getAnnotations()) {
                    types.add(a.annotationType());
                }
            }
        };
    }
}
