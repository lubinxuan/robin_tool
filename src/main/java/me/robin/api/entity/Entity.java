package me.robin.api.entity;

import org.jsoup.safety.Whitelist;

/**
 * Created by Lubin.Xuan on 2015/10/12.
 * ie.
 */
public abstract class Entity<T> {

    protected static final Whitelist NONE = Whitelist.none();

    private String key;
    private String mapping;

    protected boolean htmlClean;

    public Entity(String key) {
        this(key, key);
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

    public abstract T value(Object value);

    public Entity<T> setHtmlClean(boolean htmlClean) {
        this.htmlClean = htmlClean;
        return this;
    }
}
