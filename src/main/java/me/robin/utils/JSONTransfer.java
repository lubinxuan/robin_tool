package me.robin.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * Created by Lubin.Xuan on 2015/12/28.
 */
public class JSONTransfer<T extends Map<String, Object>> {

    private static final ThreadLocal<JSONObject> OBJECT_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL_V = new ThreadLocal<>();

    private Class<T> clz;

    public JSONTransfer(Class<T> tClass) {
        this.clz = tClass;
    }


    public void set(JSONObject tar) {
        clear();
        OBJECT_THREAD_LOCAL.set(tar);
        try {
            THREAD_LOCAL_V.set(clz.newInstance());
        } catch (InstantiationException | IllegalAccessException ignore) {

        }
    }

    public <P> void read(String key, Class<P> pClz) {
        read(key, key, pClz);
    }

    public <P> void read(String key, String alias, Class<P> pClz) {
        if (null == THREAD_LOCAL_V.get()) {
            return;
        }
        P v = OBJECT_THREAD_LOCAL.get().getObject(key, pClz);
        if (null != v) {
            THREAD_LOCAL_V.get().put(alias, v);
        }
    }

    public T get() {
        return (T) THREAD_LOCAL_V.get();
    }

    public void clear() {
        OBJECT_THREAD_LOCAL.remove();
        THREAD_LOCAL_V.remove();
    }
}
