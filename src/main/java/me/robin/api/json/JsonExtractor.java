package me.robin.api.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.robin.api.DataExtractor;
import me.robin.api.entity.CombineEntity;
import me.robin.api.entity.Context;
import me.robin.api.entity.Entity;
import me.robin.api.util.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lubin.Xuan on 2015/6/11.
 * ie.
 */
public class JsonExtractor extends DataExtractor {
    @Override
    public List<JSONObject> readData(String source, Context context) {
        String json = JsonDataUtil.read(source);
        if (null != json) {
            List<JSONObject> dataList = parseData(context, json);
            if (dataList != null) return dataList;
        }
        return Collections.emptyList();
    }

    public List<JSONObject> _readJSON(JSON source, Context context) {
        if (null == source) {
            return Collections.emptyList();
        }
        return getMapsFromJson(context, source);
    }

    protected List<JSONObject> parseData(Context context, String json) {
        Object data = JSON.parse(json);
        return getMapsFromJson(context, data);
    }

    private List<JSONObject> getMapsFromJson(Context context, Object data) {
        if (null != data && null != context && !context.isEmpty()) {
            List<JSONObject> dataList = new ArrayList<JSONObject>();
            Path.Builder builder = new Path.Builder();
            if (null != context.getTarget() && context.getTarget().length() > 0) {
                Object dataArr = PathReader.read(data, builder.eval(context.getTarget()));
                if (dataArr instanceof JSONArray) {
                    ((JSONArray) dataArr).forEach(item -> read(item, context, dataList, builder));
                } else {
                    read(dataArr, context, dataList, builder);
                }
            } else if (data instanceof JSONArray) {
                ((JSONArray) data).forEach(item -> read(item, context, dataList, builder));
            } else {
                read(data, context, dataList, builder);
            }
            return dataList;
        }
        return null;
    }

    private void read(Object data, Context context, List<JSONObject> container, Path.Builder builder) {
        JSONObject result = new JSONObject();
        for (Entity entity : context.getEntityList()) {
            if (entity instanceof CombineEntity) {
                StringBuilder sb = new StringBuilder();
                for (String key:((CombineEntity) entity).getMappings()){
                    Object val = PathReader.read(data, builder.eval(entity.mapping()));
                    if (null != val) {

                    }
                }
            } else {
                Object val = PathReader.read(data, builder.eval(entity.mapping()));
                if (null != val) {
                    result.put(entity.key(), entity.value(String.valueOf(val)));
                }
            }
        }
        if (!result.isEmpty()) {
            container.add(result);
        }
    }
}
