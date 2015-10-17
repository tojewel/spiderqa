package ru.yandex.qatools.allure.model;

import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "test-case")
@Getter
public class TestCase implements Entity {

    private String _id = UUID.randomUUID().toString();

    @Override
    public String get_id() {
        return _id;
    }

    private String executionId;

    private String threadName;

    /**
     * Inherited from TestCaseResult
     */
    @XmlElement(required = true)
    protected String name;
    protected String title;
    protected Description description;
    protected Failure failure;
    @XmlAttribute(name = "start", required = true)
    protected long start;
    @XmlAttribute(name = "stop", required = true)
    protected long stop;
    @XmlAttribute(name = "status", required = true)
    protected Status status;
    @XmlAttribute(name = "severity")
    protected SeverityLevel severity;
    @XmlElementWrapper(name = "steps")
    @XmlElement(name = "step")
    protected List<Step> steps;
    @XmlElementWrapper(name = "attachments")
    @XmlElement(name = "attachment")
    protected List<Attachment> attachments;
    @XmlElementWrapper(name = "labels")
    @XmlElement(name = "label")
    protected List<Label> labels;
    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    protected List<Parameter> parameters;

    public TestCase(Execution execution, String threadName, TestCaseResult r) {
        this.threadName = threadName;
        this.executionId = execution.get_id();

        this.name = r.name;
        this.title = r.title;
        this.description = r.description;
        this.failure = r.failure;
        this.start = r.start;
        this.stop = r.stop;
        this.status = r.status;
        this.severity = r.severity;
        this.steps = r.steps;
        this.attachments = r.attachments;
        this.labels = r.labels;
        this.parameters = r.parameters;
    }

    public TestCase() {

    }
}