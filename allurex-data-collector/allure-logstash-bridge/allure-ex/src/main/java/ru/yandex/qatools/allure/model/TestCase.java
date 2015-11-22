package ru.yandex.qatools.allure.model;

import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "test-case")
@Getter
public class TestCase implements Entity {

    private String id = UUID.randomUUID().toString().replace('-', '_');

    @Override
    public String get_id() {
        return id;
    }

    private String execution_id;

    private String host;
    private String thread;

    private String packaze;
    private String clazz;
    private String full_name;

    /**
     * Inherited from TestCaseResult
     */
    @XmlElement(required = true)
    protected String name;
    protected String title;

    protected Description description;

    @XmlElement(name = "failure")
    protected MyFailure failure;

    protected long started;
    protected long ended;
    protected Status status;
    protected SeverityLevel severity;

    protected List<Step> steps;

    protected List<Attachment> attachments;

    protected List<Label> labels;

    protected List<Parameter> parameters;

    protected List<String> issues = new ArrayList<>();

    public TestCase(Execution execution, TestSuiteResult currentTestSuite, TestCaseResult r) {
        this.execution_id = execution.get_id();

        this.name = r.name;
        this.title = r.title;
        this.description = r.description;

        if (r.failure != null) {
            this.failure = new MyFailure(r.failure);
        }

        this.started = r.start;
        this.ended = r.stop;
        this.status = r.status;
        this.severity = r.severity;
        this.steps = r.steps;
        this.attachments = r.attachments;
        this.labels = r.labels;
        this.parameters = r.parameters;

        if (r.labels != null) {
            for (Label l : r.labels) {
                if ("host".equals(l.getName())) {
                    host = ("" + l.getValue()).replace('-', '_');
                } else if ("thread".equals(l.getName())) {
                    thread = ("" + l.getValue()).replace('-', '_');
                } else if ("issue".equals(l.getName())) {
                    issues.add(l.getValue());
                }
            }
        }

        String name = currentTestSuite.getName();
        int i = name.lastIndexOf('.');

        if (i > -1) {
            this.packaze = name.substring(0, i);
            this.clazz = name.substring(i + 1);
        }
        this.full_name = name + "." + r.name;
    }

    public TestCase() {

    }
}