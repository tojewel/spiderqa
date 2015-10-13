package ru.yandex.qatools.allure.model;

import lombok.Delegate;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "test-case")
public class TestCase implements Entity {

    private String _id = UUID.randomUUID().toString();

    @Override
    public String get_id() {
        return _id;
    }

    @Getter
    private String executionId;

    @Getter
    private String threadName;

    @Delegate
    private TestCaseResult result;

    public TestCase(Execution execution, String threadName, TestCaseResult result) {
        this.threadName = threadName;
        this.executionId = execution.get_id();
        this.result = result;
    }

    public TestCase() {

    }
}