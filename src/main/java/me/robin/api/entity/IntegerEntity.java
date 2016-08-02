package me.robin.api.entity;

import com.alibaba.fastjson.util.TypeUtils;
import org.jsoup.Jsoup;

/**
 * Created by Lubin.Xuan on 2015/10/12.
 * ie.
 */
public class IntegerEntity extends Entity<Integer> {

    public IntegerEntity(String key) {
        super(key, key);
    }

    public IntegerEntity(String key, String mapping) {
        super(key, mapping);
    }

    @Override
    public Integer value(Object value) {

        if (null == value) {
            return null;
        }

        if (htmlClean && value instanceof String) {
            value = Jsoup.clean((String) value, NONE);
        }
        return TypeUtils.castToInt(value);
    }
}
