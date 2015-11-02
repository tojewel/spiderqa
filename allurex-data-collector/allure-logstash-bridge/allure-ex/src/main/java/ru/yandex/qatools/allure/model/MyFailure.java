package ru.yandex.qatools.allure.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class MyFailure {

    protected String message;
    protected String stackTrace;

    public MyFailure(Failure failure) {
        message = failure.message;
        stackTrace = failure.stackTrace;
    }

    public MyFailure() {

    }
}