package me.robin.api.entity;

import com.alibaba.fastjson.JSONArray;

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
        if (null != value && value.getClass().isArray() && ((Object[]) value).length == 0) {
            return null;
        }

        if (value instanceof JSONArray && ((JSONArray) value).isEmpty()) {
            return null;
        }

        return value;
    }
}
