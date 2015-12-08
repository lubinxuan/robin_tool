package me.robin.api.util;

import java.util.*;

/**
 * Created by Lubin.Xuan on 2015/6/12.
 * ie.
 */
public class Path {

    public static final String A_IDX_SPILT = "@";
    public static final String P_IDX_SPILT = "#";
    public static final String PATH_SPILT = "/";
    public static final String ATTR_SPILT = ":";
    public static final String ATTR_SPILT_SP = "\\:";

    private String node;
    private Integer aIdx;
    private Integer pIdx;

    public String getNode() {
        return node;
    }

    public Integer getAIdx() {
        return aIdx;
    }

    public Integer getPIdx() {
        return pIdx;
    }

    private static Path[] eval(String path) {
        String[] ps = path.split(PATH_SPILT);
        List<Path> pathList = new ArrayList<Path>();
        for (String p : ps) {
            Path pi = new Path();
            Path pai = null;
            String si = p;
            if (si.contains(ATTR_SPILT)) {
                String[] s = si.split(ATTR_SPILT_SP);
                if (s[1].trim().length() > 0) {
                    pai = new Path();
                    pai.node = "@" + s[1].trim();
                }
                si = s[0].trim();
            }
            if (si.length() > 0) {
                if (si.contains(A_IDX_SPILT)) {
                    String[] s = si.split(A_IDX_SPILT);
                    pi.node = s[0].trim();
                    try {
                        pi.aIdx = Integer.parseInt(s[1]);
                    } catch (Exception e) {
                        pi.aIdx = null;
                    }
                } else if (si.contains(P_IDX_SPILT)) {
                    String[] s = si.split(P_IDX_SPILT);
                    if (s.length > 1) {
                        pi.node = s[1].trim();
                    }
                    try {
                        pi.pIdx = Integer.parseInt(s[0]);
                    } catch (Exception e) {
                        pi.pIdx = null;
                    }
                } else {
                    pi.node = si.trim();
                }

                if (null != pi.node && pi.node.trim().length() < 1) {
                    pi.node = null;
                }
                pathList.add(pi);
            }
            if (null != pai) {
                pathList.add(pai);
            }
        }

        return pathList.toArray(new Path[pathList.size()]);
    }

    public static class Builder {

        private int cache_size = 1000;

        private Map<String, Path[]> cache = new LinkedHashMap<String, Path[]>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Path[]> entry) {
                return size() > cache_size;
            }
        };

        public Builder() {
        }

        public Builder(int cache_size) {
            this.cache_size = cache_size;
        }

        public Path[] eval(String path) {
            if (!cache.containsKey(path)) {
                synchronized (this) {
                    if (!cache.containsKey(path)) {
                        cache.put(path, Path.eval(path));
                        cache_size++;
                    }
                }
            }
            return cache.get(path);
        }
    }
}
