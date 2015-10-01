package com.spiderqa.rest;


import akka.actor.ActorSystem;
import akka.dispatch.OnSuccess;
import akka.http.javadsl.Http;
import akka.http.javadsl.OutgoingConnection;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.Authorization;
import akka.http.javadsl.model.headers.RawHeader;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import ru.yandex.qatools.allure.model.Execution;
import ru.yandex.qatools.allure.model.TestCase;
import ru.yandex.qatools.allure.model.TestSuiteResult;
import scala.concurrent.Future;

import java.util.HashMap;
import java.util.Map;

import static akka.http.javadsl.model.MediaTypes.APPLICATION_JSON;

public class RESTHeartClient {

    // Akka
    private final ActorSystem system;
    private final ActorMaterializer materializer;
    private final Flow<HttpRequest, HttpResponse, Future<OutgoingConnection>> connectionFlow;

    // DB related info, Will come from outside
    private final String database = "testdb";
    private final Class<?>[] tables = {TestCase.class, TestSuiteResult.class, Execution.class};

    private Serializer serializer = new Serializer();

    private static RESTHeartClient instance = new RESTHeartClient();

    public static RESTHeartClient get() {
        return instance;
    }

    private RESTHeartClient() {
        system = ActorSystem.create();
        materializer = ActorMaterializer.create(system);
        connectionFlow = Http.get(system).outgoingConnection("localhost", 8080);


        ddd();
    }

    private void ddd() {
        // create database
        put("/" + database, "Test data database");

        // Create tables
        for (Class<?> table : tables) {
            put("/" + database + "/" + table.getSimpleName(), "Table created");
        }
    }

    private void put(String url, String desc) {
        submit(HttpRequest
                .PUT(url)
                .withEntity(APPLICATION_JSON.toContentType(), "{\"description\": \"" + desc + "\"}"));
    }

    private Map<Integer, ObjectStatus> status = new HashMap<>();

    class ObjectStatus {
        private String etag;
        private Object dataObject;
        private boolean savingInProgress = true;

        public void setEtag(String etag) {
            this.etag = etag;
        }

        public String getEtag() {
            return etag;
        }

        public void setPending(Object dataObject) {
            this.dataObject = dataObject;
        }

        public Object getPending() {
            return dataObject;
        }

        public void setSavingInProgress(boolean savingInProgress) {
            this.savingInProgress = savingInProgress;
        }

        public boolean isSavingInProgress() {
            return savingInProgress;
        }
    }

    public void save(final Object dataObject) {

        // FIXME MAJOR PROBLEM WITH THREADING
        // This thread and akka returning thread is different. resolve race conditions
        synchronized (dataObject) {
            ObjectStatus objectStatus = status.get(dataObject.hashCode());

            // New object
            if (objectStatus == null) {
                status.put(dataObject.hashCode(), objectStatus = new ObjectStatus());
            }

            // First saving in progress...
            else if (objectStatus.isSavingInProgress()) {
                System.out.println("First saving in progress...");
                objectStatus.setPending(dataObject);
                return;
            }

            HttpRequest httpRequest = HttpRequest
                    .POST("/" + database + "/" + dataObject.getClass().getSimpleName())
                    .withEntity(APPLICATION_JSON.toContentType(), serializer.toJson(dataObject))
                    // .addHeader(Authorization.basic("a", "a"))
                     ;

            if (objectStatus.getEtag() != null) {
                httpRequest = httpRequest.addHeader(RawHeader.create("If-Match", objectStatus.getEtag()));
            }

            final HttpRequest httpRequest2 = httpRequest;

            Source.single(httpRequest)
                    .via(connectionFlow)
                    .runWith(Sink.<HttpResponse>head(), materializer)
                    .onSuccess(new OnSuccess<HttpResponse>() {
                        @Override
                        public void onSuccess(HttpResponse result) throws Throwable {
                            System.out.println("<<<RESPONSE (" + result.status() + "): " + httpRequest2.getUri());
                            synchronized (dataObject) {
                                ObjectStatus objectStatus = status.get(dataObject.hashCode());
                                objectStatus.setSavingInProgress(false);

                                Object etag = result.getHeader("etag").getOrElse(null);
                                objectStatus.setEtag("" + etag);

                                if (objectStatus.getPending() != null) {
                                    save(objectStatus.getPending());
                                    objectStatus.setPending(null);
                                }
                            }
                        }
                    }, materializer.executionContext());

            System.out.println(">>>REQUEST: " + httpRequest.getUri());
        }
    }

    private void submit(final HttpRequest httpRequest) {
        final long tname = Thread.currentThread().getId();

        Source.single(httpRequest)
                .via(connectionFlow)
                .runWith(Sink.<HttpResponse>head(), materializer)
                .onSuccess(new OnSuccess<HttpResponse>() {
                    @Override
                    public void onSuccess(HttpResponse result) throws Throwable {

                        System.out.println("<<<RESPONSE(" + tname + ") (" + result.status() + "): " + httpRequest.getUri());
//                        for (HttpHeader httpHeader : result.getHeaders()) {
//                            System.out.println(httpHeader.name() + "=" + httpHeader.value());
//                        }
                    }
                }, materializer.executionContext());

        System.out.println(">>>REQUEST(" + tname + "): " + httpRequest.getUri());
    }
}
