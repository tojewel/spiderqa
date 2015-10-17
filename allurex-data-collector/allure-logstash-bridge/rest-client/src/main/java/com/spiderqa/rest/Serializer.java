package com.spiderqa.rest;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import ru.yandex.qatools.allure.model.TestCaseResult;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Function;

public class Serializer {
    private final Map<Class<?>, Marshaller> marshallers = new HashMapPlus(new Function<Class<?>, Marshaller>() {
        public Marshaller apply(Class<?> aClass) {
            return createMarshaller(aClass);
        }
    });

    public Serializer() {
        createMarshaller(TestCaseResult.class);
    }

    private Marshaller createMarshaller(Class clazz) {
        try {
            Marshaller marshaller = JAXBContext.newInstance(clazz).createMarshaller();
            marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
            marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            return marshaller;
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String toJson(Object dataObject) {
        StringWriter sw = new StringWriter();
        try {
            marshallers.get(dataObject.getClass()).marshal(dataObject, sw);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return sw.toString();
    }
}