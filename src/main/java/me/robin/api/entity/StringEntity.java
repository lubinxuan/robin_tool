package me.robin.api.entity;

import org.apache.commons.lang.StringUtils;

/**
 * Created by Lubin.Xuan on 2015/10/12.
 * ie.
 */
public class StringEntity extends Entity<String> {

    public StringEntity(String key) {
        super(key, key);
    }

    public StringEntity(String key, String mapping) {
        super(key, mapping);
    }

    @Override
    public String value(Object value) {
        if (null != value && value.getClass().isArray()) {
            return StringUtils.join((Object[]) value, ",");
        }
        return String.valueOf(value);
    }
}
