package me.robin.api.util;

import me.robin.api.entity.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2015/11/16.
 */
public class ContextBuilder {
    List<Entity> el = new ArrayList<>();

    Set<String> filter = new HashSet<>();

    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    public ContextBuilder add(Entity entity) {
        if (filter.add(entity.key())) {
            el.add(entity);
        }
        return this;
    }

    public ContextBuilder string(String key, String mapping) {
        if (filter.add(key)) {
            el.add(new StringEntity(key, mapping));
        }
        return this;
    }

    public ContextBuilder string(String key) {
        return string(key, key);
    }

    public ContextBuilder integer(String key, String mapping) {
        if (filter.add(key)) {
            el.add(new IntegerEntity(key, mapping));
        }
        return this;
    }

    public ContextBuilder integer(String key) {
        return integer(key, key);
    }

    public ContextBuilder date(String key, String mapping, String dateFormat) {
        if (filter.add(key)) {
            el.add(new DateEntity(key, mapping, dateFormat));
        }
        return this;
    }

    public ContextBuilder date(String key, String dateFormat) {
        return date(key, key, dateFormat);
    }

    public ContextBuilder combine(String key, String[] mappings, String join) {
        if (filter.add(key)) {
            if (null == join) {
                el.add(new CombineEntity(key, mappings));
            } else {
                el.add(new CombineEntity(key, mappings, join));
            }
        }
        return this;
    }

    public ContextBuilder combine(String key, String[] mappings) {
        return combine(key, mappings, null);
    }

    public void reset() {
        el.clear();
    }

    public Context build(String rowPath) {
        return new Context(rowPath, new ArrayList<>(el));
    }

    public Context build() {
        return new Context(new ArrayList<>(el));
    }
}
