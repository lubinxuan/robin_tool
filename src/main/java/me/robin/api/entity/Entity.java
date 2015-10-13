package me.robin.api.entity;

/**
 * Created by Lubin.Xuan on 2015/10/12.
 * ie.
 */
public abstract class Entity<T> {

    private String key;
    private String mapping;

    public Entity(String key) {
        this(key,key);
    }

    public Entity(String key, String mapping) {
        this.key = key;
        this.mapping = mapping;
    }

    public String key() {
        return key;
    }

    public String mapping() {
        return mapping;
    }

    public abstract T value(String value);
}
