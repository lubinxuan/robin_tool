package me.robin.hbase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Lubin.Xuan on 2014/12/15.
 * ie.
 */
public class CollectionStore {
    private static final Map<String, HBaseSolrData> hBaseSolrDataMap = new HashMap<>();

    private static final Set<String> FAILED_INITIALIZE_CORE = new HashSet<>();

    private synchronized static void initialize(String core) {
        if (hBaseSolrDataMap.containsKey(core) || FAILED_INITIALIZE_CORE.contains(core)) {
            return;
        }
        try {
            hBaseSolrDataMap.put(core, new HBaseSolrData(Configure.getConfiguration(), core));
        } catch (Exception e) {
            e.printStackTrace();
            FAILED_INITIALIZE_CORE.add(core);
        }
    }

    public static HBaseSolrData get(String core) {
        if (null == core || core.trim().length() < 1) {
            return null;
        }
        if (!hBaseSolrDataMap.containsKey(core) && !FAILED_INITIALIZE_CORE.contains(core)) {
            initialize(core);
        }
        return hBaseSolrDataMap.get(core);
    }

}
