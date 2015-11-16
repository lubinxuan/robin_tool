package me.robin.solr.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by Lubin.Xuan on 2015/10/16.
 * ie.
 */
public class HBaseDataThreadUtil {

    private static class HBaseRequestInfo {
        Map<String, Map<String, Object>> data = Collections.emptyMap();
        Map<Integer, String> dataMap = Collections.emptyMap();
        Set<String> field = Collections.emptySet();

        public HBaseRequestInfo(Map<String, Map<String, Object>> data, Map<Integer, String> dataMap, Set<String> field) {
            this.data = data;
            this.dataMap = dataMap;
            this.field = field;
        }

        public Map<String, Map<String, Object>> getData() {
            return data;
        }

        public Map<Integer, String> getDataMap() {
            return dataMap;
        }

        public Set<String> getField() {
            return field;
        }
    }

    private static HBaseRequestInfo EMPTY = new HBaseRequestInfo(Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet());

    private static final ThreadLocal<HBaseRequestInfo> H_BASE_REQUEST_INFO_THREAD_LOCAL = new ThreadLocal<>();

    public static void data(Map<String, Map<String, Object>> data, Map<Integer, String> data_map, Set<String> field) {
        H_BASE_REQUEST_INFO_THREAD_LOCAL.set(new HBaseRequestInfo(data, data_map, field));
    }

    private static HBaseRequestInfo get() {
        HBaseRequestInfo curr = H_BASE_REQUEST_INFO_THREAD_LOCAL.get();
        if (null == curr) {
            return EMPTY;
        } else {
            return curr;
        }
    }

    public static Set<String> field() {
        return get().getField();
    }

    public static Map<String, Map<String, Object>> data() {
        return get().getData();
    }

    public static Map<String, Object> getDoc(String uniqueKey) {
        return get().getData().get(uniqueKey);
    }

    public static Map<String, Object> getDoc(int id) {

        String uid = get().getDataMap().get(id);
        if (null == uid) {
            return null;
        }
        Map<String, Map<String, Object>> data = get().getData();
        return data.get(uid);
    }

    public static void clear() {
        H_BASE_REQUEST_INFO_THREAD_LOCAL.remove();
    }

}
