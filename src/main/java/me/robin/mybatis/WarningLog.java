package me.robin.mybatis;

import java.io.Serializable;

/**
 * Created by Lubin.Xuan on 2015/9/2.
 * ie.
 */
public class WarningLog implements Serializable {
    private long last = 0l;

    private long period = 1000 * 60;

    private int count = 0;

    private int resetCount = 0;

    public synchronized boolean send() {
        long now = System.currentTimeMillis();
        long between = now - last;
        boolean send = false;
        if (last == 0l) {
            send = true;
        } else if (between > period) {
            send = true;
        }
        count++;
        if (send) {
            last = now;
            resetCount++;
            if (resetCount == 5) {
                resetCount = 0;
                period = period << 1;
            }
        }
        return send;
    }

    public synchronized int errorCount() {
        return count;
    }
}
