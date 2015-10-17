package ru.yandex.qatools.allure.model;

import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "test-suite")
@Getter
public class TestSuite implements Entity {

    private String _id = UUID.randomUUID().toString().replace('-', '_');
    private String execution_id;

    @Override
    public String get_id() {
        return _id;
    }

    @XmlElement(required = true)
    protected String name;
    protected String title;
    protected Description description;
    @XmlAttribute(name = "start", required = true)
    protected long start;
    @XmlAttribute(name = "stop", required = true)
    protected long stop;
    @XmlAttribute(name = "version")
    protected String version;
    @XmlElementWrapper(name = "labels")
    @XmlElement(name = "label")
    protected List<Label> labels;

    public TestSuite(Execution execution, TestSuiteResult r) {
        this.execution_id = execution.get_id();

        this.name = r.name;
        this.title = r.title;
        this.description = r.description;
        this.start = r.start;
        this.stop = r.stop;
        this.labels = r.labels;
        this.version = r.version;
    }

    public TestSuite() {

    }
}