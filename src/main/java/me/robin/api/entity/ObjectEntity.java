package me.robin.api.entity;

/**
 * Created by Lubin.Xuan on 2015/12/15.
 */
public class ObjectEntity extends Entity {
    public ObjectEntity(String key) {
        super(key);
    }

    public ObjectEntity(String key, String mapping) {
        super(key, mapping);
    }

    @Override
    public Object value(Object value) {
        return value;
    }
}
