package me.robin.utils;

/**
 * Created by Lubin.Xuan on 2015/12/23.
 */
public class CloseableUtils {
    public static void closeQuietly(AutoCloseable closeable) {
        if (null == closeable) {
            return;
        }

        try {
            closeable.close();
        } catch (Throwable ignore) {

        }
    }
}
