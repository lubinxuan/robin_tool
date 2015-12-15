package me.robin.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.robin.api.entity.Context;
import me.robin.api.json.JsonDataUtil;
import me.robin.api.json.JsonExtractor;
import me.robin.api.util.Type;
import me.robin.api.xml.XmlExtractor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lubin.Xuan on 2015/6/11.
 * ie.
 */
public abstract class DataExtractor {

    public abstract List<JSONObject> readData(String source, Context context);

    private static final Map<Type, DataExtractor> DATA_EXTRACTOR_MAP = new ConcurrentHashMap<>();
    private final static DataExtractor json = new JsonExtractor();

    static {
        DATA_EXTRACTOR_MAP.put(Type.JSON, json);
        DATA_EXTRACTOR_MAP.put(Type.JSONP, json);
        DATA_EXTRACTOR_MAP.put(Type.XML, new XmlExtractor());
    }

    public static List<JSONObject> readData(Type type, String source, Context context) {
        if (DATA_EXTRACTOR_MAP.containsKey(type)) {
            if (Type.JSONP.equals(type)) {
                source = JsonDataUtil.read(source);
            }
            List<JSONObject> readData = DATA_EXTRACTOR_MAP.get(type).readData(source, context);
            return null == readData ? Collections.emptyList() : readData;
        } else {
            return Collections.emptyList();
        }
    }

    public static List<JSONObject> readJSON(JSON source, Context context) {
        List<JSONObject> readData = ((JsonExtractor) json)._readJSON(source, context);
        return null == readData ? Collections.emptyList() : readData;
    }

    public static JSONObject readJSONFirst(JSON source, Context context) {
        List<JSONObject> readData = ((JsonExtractor) json)._readJSON(source, context);
        return null != readData && !readData.isEmpty() ? readData.get(0) : new JSONObject();
    }

}
