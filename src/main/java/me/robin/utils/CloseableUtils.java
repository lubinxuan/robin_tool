package me.robin.utils;

import java.io.Closeable;

/**
 * Created by Lubin.Xuan on 2015/12/23.
 */
public class CloseableUtils {
    public static void closeQuietly(Closeable closeable) {
        if (null == closeable) {
            return;
        }

        try {
            closeable.close();
        } catch (Throwable ignore) {

        }
    }
}
