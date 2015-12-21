package me.robin.extension;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lubin.Xuan on 2015/12/21.
 */
public class ExtensionServiceLoader<T> {

    private static final String EXTENSION_DIR = "META-INF/extension";

    private final Map<String, T> serviceMap = new HashMap<>();

    private boolean inited = false;


    private void _init(Class<T> tClass) {
        if (!inited) {
            synchronized (this) {
                if (!inited) {
                    String file = EXTENSION_DIR + "/" + tClass.getName();
                    try {
                        InputStream is = ExtensionServiceLoader.class.getClassLoader().getResourceAsStream(file);
                        List<String> list = IOUtils.readLines(is, "utf-8");
                        for (String line : list) {
                            line = line.trim();
                            if (line.startsWith("#")) {
                                continue;
                            }
                            String[] sp = line.split("=");
                            String name = sp[0];
                            String clazz = sp[1];
                            try {
                                Class<T> c = (Class<T>) Class.forName(clazz);
                                T t = c.newInstance();
                                serviceMap.put(name, t);
                            } catch (Throwable ignore) {

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    inited = true;
                }
            }
        }
    }

    public Map<String, T> load() {
        return serviceMap;
    }

    public T load(String name) {
        return serviceMap.get(name);
    }

    private static final Map<String, ExtensionServiceLoader> LOADER_MAP = new ConcurrentHashMap<>();

    private static <T> ExtensionServiceLoader<T> create(Class<T> tClass) {
        ExtensionServiceLoader<T> serviceLoader = LOADER_MAP.get(tClass.getName());
        if (null == serviceLoader) {
            serviceLoader = new ExtensionServiceLoader<>();
            serviceLoader._init(tClass);
            LOADER_MAP.put(tClass.getName(), serviceLoader);
        }
        return serviceLoader;
    }

    public static <T> T loadService(Class<T> tClass, String name) {
        return create(tClass).load(name);
    }

    public static <T> Map<String, T> loadService(Class<T> tClass) {
        return create(tClass).load();
    }
}
