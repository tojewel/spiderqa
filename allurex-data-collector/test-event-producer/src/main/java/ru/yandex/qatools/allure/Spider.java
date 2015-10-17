package ru.yandex.qatools.allure;

import com.spiderqa.rest.RESTClient;
import com.spiderqa.rest.Serializer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.yandex.qatools.allure.events.TestCaseFinishedEvent;
import ru.yandex.qatools.allure.events.TestCaseStartedEvent;
import ru.yandex.qatools.allure.model.Execution;
import ru.yandex.qatools.allure.model.TestCase;
import ru.yandex.qatools.allure.model.TestCaseResult;

public class Spider extends Allure {
    private static Spider instance = new Spider();
    private final String threadName = "thread_" + System.nanoTime();

    private TestCaseResult testCaseResult;

    public static Spider get() {
        return instance;
    }

    private Spider() {
        super();
    }

    public void fire(TestCaseStartedEvent event) {
        super.fire(event);
        testCaseResult = getTestCaseStorage().get();
    }

    private Serializer serializer = new Serializer();
    private static Execution execution = new Execution();

    public void fire(TestCaseFinishedEvent event) {

        super.fire(event);
        System.out.println("testCaseResult=" + ToStringBuilder.reflectionToString(testCaseResult, ToStringStyle.MULTI_LINE_STYLE));
        System.out.println("testCaseResult=" + serializer.toJson(testCaseResult));
        TestCase testCase = new TestCase(execution, threadName, testCaseResult);

        System.out.println("testCase=" + ToStringBuilder.reflectionToString(testCase, ToStringStyle.MULTI_LINE_STYLE));
        System.out.println("testCase=" + serializer.toJson(testCase));

        RESTClient.get().save(testCase);
    }
}