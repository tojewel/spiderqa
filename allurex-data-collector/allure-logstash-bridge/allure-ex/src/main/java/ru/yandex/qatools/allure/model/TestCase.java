package ru.yandex.qatools.allure.model;

import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "test-case")
@Getter
public class TestCase implements Entity {

    private String _id = UUID.randomUUID().toString().replace('-', '_');

    @Override
    public String get_id() {
        return _id;
    }

    private String execution_id;

    private String host;
    private String thread;

    private String packaze;
    private String clazz;

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

    public TestCase(Execution execution, TestSuiteResult currentTestSuite, TestCaseResult r) {
        this.execution_id = execution.get_id();

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

        for (Label l : r.labels) {
            if ("host".equals(l.getName())) {
                host = ("" + l.getValue()).replace('-', '_');
            } else if ("thread".equals(l.getName())) {
                thread = ("" + l.getValue()).replace('-', '_');
            }
        }

        String name = currentTestSuite.getName();
        int i = name.lastIndexOf('.');
        this.packaze = name.substring(0, i);
        this.clazz = name.substring(i + 1);
    }

    public TestCase() {

    }
}