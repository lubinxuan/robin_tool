package me.robin.api.entity;

/**
 * Created by Lubin.Xuan on 2015/10/12.
 * ie.
 */
public class StringEntity extends Entity<String> {

    public StringEntity(String key) {
        super(key,key);
    }

    public StringEntity(String key, String mapping) {
        super(key, mapping);
    }

    @Override
    public String value(String value) {
        return value;
    }
}
