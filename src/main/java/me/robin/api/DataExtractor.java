package me.robin.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.robin.api.entity.Context;
import me.robin.api.json.JsonExtractor;
import me.robin.api.util.Type;
import me.robin.api.xml.XmlExtractor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Created by Lubin.Xuan on 2015/6/11.
 * ie.
 */
public abstract class DataExtractor {

    public abstract List<JSONObject> readData(String source, Context context);

    private static final Map<Type, DataExtractor> DATA_EXTRACTOR_MAP = new ConcurrentHashMap<>();

    private static DataExtractor get(Type type) {
        return DATA_EXTRACTOR_MAP.computeIfAbsent(type, type1 -> {
            if (Type.JSON.equals(type1)) {
                return new JsonExtractor();
            }
            if (Type.JSONP.equals(type1)) {
                return new JsonExtractor();
            }
            if (Type.XML.equals(type1)) {
                return new XmlExtractor();
            }
            return null;
        });
    }

    public static List<JSONObject> readData(Type type, String source, Context context) {
        List<JSONObject> readData = get(type).readData(source, context);
        return null == readData ? Collections.emptyList() : readData;
    }


    public static List<JSONObject> readJSON(JSON source, Context context) {
        List<JSONObject> readData = ((JsonExtractor) get(Type.JSON))._readJSON(source, context);
        return null == readData ? Collections.emptyList() : readData;
    }

    public static JSONObject readJSONFirst(JSON source, Context context) {
        List<JSONObject> readData = readJSON(source, context);
        return null != readData && !readData.isEmpty() ? readData.get(0) : new JSONObject();
    }

    public static List<JSONObject> readJSON(String source, Context context) {
        return get(Type.JSON).readData(source, context);
    }

    public static JSONObject readJSONFirst(String source, Context context) {
        List<JSONObject> readData = readJSON(source, context);
        return null != readData && !readData.isEmpty() ? readData.get(0) : new JSONObject();
    }

}
