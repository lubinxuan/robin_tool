package me.robin.api.entity;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

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

        if (null == value) {
            return null;
        }

        if (value.getClass().isArray()) {
            return StringUtils.join((Object[]) value, ",");
        }

        if (value instanceof JSONArray && ((JSONArray) value).isEmpty()) {
            return null;
        }


        String _value = String.valueOf(value);

        if (StringUtils.isBlank(_value)) {
            return null;
        }

        if (htmlClean) {
            return Jsoup.clean(_value, NONE);
        }

        return _value;
    }
}
