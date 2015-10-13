package me.robin.api.json;

/**
 * Created by Lubin.Xuan on 2015/6/11.
 * ie.
 */
public class JsonDataUtil {
    public static String read(String jsonStr) {

        if (null == jsonStr || jsonStr.trim().length() < 1) {
            return null;
        }
        jsonStr = jsonStr.trim();
        if (jsonStr.endsWith(";")) {
            jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
        }
        jsonStr = jsonStr.trim();
        if (jsonStr.endsWith(")")) {
            int startBracket = jsonStr.indexOf("(") + 1;
            return jsonStr.substring(startBracket, jsonStr.length() - 1);
        } else {
            return jsonStr;
        }
    }
}
