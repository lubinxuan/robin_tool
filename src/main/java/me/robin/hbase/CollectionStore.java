package me.robin.hbase;

import me.robin.solr.util.HBaseSolrData;

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

    public static boolean initialize(String core) {
        return initialize(core, 1);
    }

    public synchronized static boolean initialize(String core, int regionCount) {

        if (FAILED_INITIALIZE_CORE.contains(core)) {
            return false;
        }

        if (hBaseSolrDataMap.containsKey(core)) {
            return true;
        }
        try {
            if (regionCount > 3) {
                hBaseSolrDataMap.put(core, new HBaseSolrData(Configure.getConfiguration(), core, regionCount));
            } else {
                hBaseSolrDataMap.put(core, new HBaseSolrData(Configure.getConfiguration(), core));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            FAILED_INITIALIZE_CORE.add(core);
            return false;
        }
    }

    public static boolean failCore(String core) {
        return FAILED_INITIALIZE_CORE.contains(core);
    }

    public static HBaseSolrData get(String core) {
        if (null == core || core.trim().length() < 1) {
            return null;
        }
        return hBaseSolrDataMap.get(core);
    }

}
