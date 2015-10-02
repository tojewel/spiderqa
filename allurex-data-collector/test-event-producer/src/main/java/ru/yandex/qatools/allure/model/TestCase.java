package ru.yandex.qatools.allure.model;

import lombok.Delegate;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "test-case")
public class TestCase {

    @Getter
    private String _id = UUID.randomUUID().toString();

    @Getter
    private String executionId;

    @Delegate
    private TestCaseResult result;

    public TestCase(Execution execution, TestCaseResult result) {
        this.executionId = execution.get_id();
        this.result = result;
    }

    public TestCase() {
    }
}