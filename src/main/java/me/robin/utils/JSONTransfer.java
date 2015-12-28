package me.robin.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

/**
 * Created by Lubin.Xuan on 2015/12/28.
 */
public class JSONTransfer<T extends HashMap<String, Object>> {

    private static final ThreadLocal<JSONObject> OBJECT_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<HashMap<String, Object>> THREAD_LOCAL_V = new ThreadLocal<>();

    private Class<T> clz;

    public JSONTransfer(Class<T> tClass) {
        this.clz = tClass;
    }


    public void set(JSONObject tar) throws IllegalAccessException, InstantiationException {
        clear();
        OBJECT_THREAD_LOCAL.set(tar);
        THREAD_LOCAL_V.set(clz.newInstance());
    }

    public <P> void read(String key, Class<P> pClz) {
        read(key, key, pClz);
    }

    public <P> void read(String key, String alias, Class<P> pClz) {
        P v = OBJECT_THREAD_LOCAL.get().getObject(key, pClz);
        if (null != v) {
            THREAD_LOCAL_V.get().put(alias, v);
        }
    }

    public void clear() {
        OBJECT_THREAD_LOCAL.remove();
        THREAD_LOCAL_V.remove();
    }
}
