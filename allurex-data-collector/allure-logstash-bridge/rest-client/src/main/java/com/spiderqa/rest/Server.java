package com.spiderqa.rest;

import ru.yandex.qatools.allure.model.Entity;

public interface Server {
    int getPort();

    String upsertURL(Entity e);
}

class MongoServer implements Server {
    final int port = 8080;
    private final String database = "testaspect";

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String upsertURL(Entity e) {
        return "/" + database + "/" + e.getClass().getSimpleName();
    }
}

class ElasticServer implements Server {
    int port = 9200;
    private String database = "testaspect";

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String upsertURL(Entity e) {
        return "/" + database + "/" + e.getClass().getSimpleName() + "/" + e.get_id();
    }
}
