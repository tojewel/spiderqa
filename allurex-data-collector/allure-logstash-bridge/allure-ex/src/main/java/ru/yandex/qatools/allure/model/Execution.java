package ru.yandex.qatools.allure.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "test-case")
public class Execution {

    private String _id = UUID.randomUUID().toString().replace('-', '_');

    public Execution() {

    }

    public String get_id() {
        return _id;
    }
}
