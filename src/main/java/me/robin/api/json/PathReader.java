package me.robin.api.json;

import me.robin.api.util.Path;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by Lubin.Xuan on 2015/6/11.
 * ie.
 */
public class PathReader {

    public static Object read(Object source, Path[] paths) {
        return read(source, paths, 0);
    }

    private static Object read(Object source, Path[] path, int depth) {
        if (path.length > depth) {
            if (source instanceof JSONArray) {
                return readArray((JSONArray) source, path, depth);
            } else if (source instanceof JSONObject) {
                return readObject((JSONObject) source, path, depth);
            } else {
                return null;
            }
        } else {
            return source;
        }
    }

    private static Object readArray(JSONArray source, Path[] path, int depth) {
        if (path.length > depth) {
            Path pi = path[depth];
            if (pi.getPIdx() != null && pi.getPIdx() < source.size()) {
                return read(source.get(pi.getPIdx()), path, null != pi.getNode() ? depth : depth + 1);
            } else {
                return null;
            }
        } else {
            return source;
        }
    }

    private static Object readObject(JSONObject source, Path[] path, int depth) {
        if (path.length > depth) {
            Path pi = path[depth];
            if (null != pi.getNode()) {
                Object data = source.get(pi.getNode());
                if (null != pi.getAIdx()) {
                    if (data instanceof JSONArray) {
                        if (pi.getAIdx() < ((JSONArray) data).size()) {
                            return read(((JSONArray) data).get(pi.getAIdx()), path, depth + 1);
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return read(data, path, depth + 1);
                }
            } else {
                return null;
            }
        } else {
            return source;
        }
    }
}
