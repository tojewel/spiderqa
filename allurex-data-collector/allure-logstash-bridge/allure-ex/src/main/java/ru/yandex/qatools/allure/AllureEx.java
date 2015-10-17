package ru.yandex.qatools.allure;

import com.spiderqa.rest.RESTClient;
import com.spiderqa.rest.Serializer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.yandex.qatools.allure.events.TestCaseFinishedEvent;
import ru.yandex.qatools.allure.events.TestCaseStartedEvent;
import ru.yandex.qatools.allure.events.TestSuiteEvent;
import ru.yandex.qatools.allure.events.TestSuiteFinishedEvent;
import ru.yandex.qatools.allure.model.*;

public class AllureEx extends Allure {
    private static AllureEx instance = new AllureEx();

    private static Execution execution = new Execution();
    private TestSuiteResult currentTestSuite;

    // FIXME Watch out for multi-threaded test
    public static AllureEx get() {
        return instance;
    }

    public void fire(TestSuiteEvent event) {
        super.fire(event);
        this.currentTestSuite = getTestSuiteStorage().get(event.getUid());
        System.out.println("TestSuiteEvent=" + currentTestSuite.getName());
    }

    public void fire(TestSuiteFinishedEvent event) {
        TestSuiteResult testSuite = getTestSuiteStorage().get(event.getUid());
        if (testSuite == null) {
            return;
        }

        super.fire(event);
        RESTClient.get().save(new TestSuite(execution, testSuite));
        System.out.println("TestSuiteFinishedEvent=" + testSuite.getName());
    }

//    public void fire(TestCaseStartedEvent event) {
//        super.fire(event);
//    }

    public void fire(TestCaseFinishedEvent event) {
        TestCaseResult testCaseResult = getTestCaseStorage().get();

        super.fire(event);

        RESTClient.get().save(new TestCase(execution, currentTestSuite, testCaseResult));
        System.out.println("TestCaseFinishedEvent=" + testCaseResult.getName());
    }
}