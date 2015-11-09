package com.spiderqa.rest;

import akka.actor.ActorSystem;
import akka.dispatch.OnSuccess;
import akka.http.javadsl.Http;
import akka.http.javadsl.OutgoingConnection;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.RawHeader;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import ru.yandex.qatools.allure.model.Entity;
import scala.concurrent.Future;

import java.util.HashMap;
import java.util.Map;

import static akka.http.javadsl.model.MediaTypes.APPLICATION_JSON;

public class RESTClient {

    // Akka
    private final ActorSystem system;
    private final ActorMaterializer materializer;
    private final Flow<HttpRequest, HttpResponse, Future<OutgoingConnection>> connectionFlow;

    private final Server server;

    private Serializer serializer = new Serializer();

    private static RESTClient instance = new RESTClient();

    public static RESTClient get() {
        return instance;
    }

    private RESTClient() {
        system = ActorSystem.create();
        materializer = ActorMaterializer.create(system);

        //server = new ElasticServer();
        server = new MongoServer();

        connectionFlow = Http.get(system).outgoingConnection("localhost", server.getPort());
    }

    private Map<Integer, ObjectStatus> status = new HashMap<>();

    // TODO move into Entity interface; DDD ;)
    class ObjectStatus {
        private String etag;
        private Entity dataObject;
        private boolean savingInProgress = true;

        public void setEtag(String etag) {
            this.etag = etag;
        }

        public String getEtag() {
            return etag;
        }

        public void setPending(Entity dataObject) {
            this.dataObject = dataObject;
        }

        public Entity getPending() {
            return dataObject;
        }

        public void setSavingInProgress(boolean savingInProgress) {
            this.savingInProgress = savingInProgress;
        }

        public boolean isSavingInProgress() {
            return savingInProgress;
        }
    }

    public void save(final Entity entity) {

        // FIXME MAJOR PROBLEM WITH THREADING
        // This thread and akka returning thread is different. resolve race conditions
        synchronized (entity) {
            ObjectStatus objectStatus = status.get(entity.hashCode());

            // New object
            if (objectStatus == null) {
                status.put(entity.hashCode(), objectStatus = new ObjectStatus());
            }

            // First saving in progress...
            else if (objectStatus.isSavingInProgress()) {
                System.out.println("First saving in progress...");
                objectStatus.setPending(entity);
                return;
            }

            HttpRequest httpRequest = HttpRequest
                    // .POST("/" + database + "/" + entity.getClass().getSimpleName())
                    .POST(server.upsertURL(entity))
                    .withEntity(APPLICATION_JSON.toContentType(), serializer.toJson(entity))
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
                            synchronized (entity) {
                                ObjectStatus objectStatus = status.get(entity.hashCode());
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
}
