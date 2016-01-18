package me.robin.utils;

/**
 * Created by Lubin.Xuan on 2015/1/5.
 */
public class TimeTicker {

    private long now = System.currentTimeMillis();

    public long time() {
        long pas = System.currentTimeMillis() - now;
        now = System.currentTimeMillis();
        return pas;
    }
}
