package me.robin.solr.util;

import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lubin.Xuan on 2015/10/16.
 * ie.
 */
public class SolrSchemeUtil {
    private static final Map<String, Map<String, SchemaField>> SCHEMA_CACHE = new ConcurrentHashMap<>();

    public static Map<String, SchemaField> schemeInfo(String coreName, IndexSchema indexSchema) {
        return SCHEMA_CACHE.compute(coreName, (s, stringSchemaFieldMap) -> {
            if (null == stringSchemaFieldMap) {
                return indexSchema.getFields();
            }
            return stringSchemaFieldMap;
        });
    }

    public static Map<String, SchemaField> schemeInfo(String coreName) {
        return SCHEMA_CACHE.get(coreName);
    }

}
