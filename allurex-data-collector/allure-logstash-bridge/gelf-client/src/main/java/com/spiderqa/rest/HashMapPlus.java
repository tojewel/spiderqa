package com.spiderqa.rest;

import java.util.HashMap;
import java.util.function.Function;

public class HashMapPlus<K, V> extends HashMap<K, V> {
    private Function<K, V> creator;

    public HashMapPlus(Function<K, V> creator) {
        this.creator = creator;
    }

    @Override
    public V get(Object key) {
        V v = super.get(key);
        if (v == null) {
            put((K) key, v = creator.apply((K) key));
        }
        return v;
    }
}
