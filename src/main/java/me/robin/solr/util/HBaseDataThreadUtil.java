package me.robin.solr.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by Lubin.Xuan on 2015/10/16.
 * ie.
 */
public class HBaseDataThreadUtil {
    private static final ThreadLocal<Map<String, Map<String, Object>>> DATA = new ThreadLocal<>();
    private static final ThreadLocal<Map<Integer, String>> DATA_MAP = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> FIELD = new ThreadLocal<>();

    public static void data(Map<String, Map<String, Object>> data, Map<Integer, String> data_map, Set<String> field) {
        DATA.set(Collections.unmodifiableMap(data));
        DATA_MAP.set(Collections.unmodifiableMap(data_map));
        FIELD.set(field);
    }

    public static Set<String> field() {
        return FIELD.get();
    }

    public static Map<String, Map<String, Object>> data() {
        return DATA.get();
    }

    public static Map<String, Object> getDoc(String uniqueKey) {
        Map<String, Map<String, Object>> data = DATA.get();
        if (null != data) {
            return data.get(uniqueKey);
        } else {
            return null;
        }
    }

    public static Map<String, Object> getDoc(int id) {

        Map<Integer, String> idMap = DATA_MAP.get();
        if (null == idMap) {
            return null;
        } else {
            String uid = DATA_MAP.get().get(id);
            if (null == uid) {
                return null;
            }
            Map<String, Map<String, Object>> data = DATA.get();
            if (null != data) {
                return data.get(uid);
            } else {
                return null;
            }
        }
    }

    public static void clear() {
        DATA.remove();
        FIELD.remove();
        DATA_MAP.remove();
    }

}
