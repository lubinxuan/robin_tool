package me.robin.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lubin.Xuan on 2015/10/12.
 * ie.
 */
public class RegularUtil {

    private static final Map<String, Pattern> CACHE = new LinkedHashMap<String, Pattern>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Pattern> entry) {
            return this.size() > 10000;
        }
    };

    public static String regVal(String str, String reg, int group) {
        Pattern pattern = CACHE.computeIfAbsent(reg, Pattern::compile);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(group);
        } else {
            return null;
        }
    }

    public static Map<Integer, String> regVal(String str, String reg) {
        Pattern pattern = CACHE.computeIfAbsent(reg, Pattern::compile);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            Map<Integer, String> map = new HashMap<>();
            for (int i = 0; i < matcher.groupCount(); i++) {
                map.put(i, matcher.group(i));
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }
}
