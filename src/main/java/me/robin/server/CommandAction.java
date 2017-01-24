package me.robin.server;

/**
 * Created by xuanlubin on 2017/1/23.
 */
public interface CommandAction {
    default boolean accept(String command) {
        return false;
    }

    default String desc() {
        return "desc for command:" + this;
    }

    Object process(String command);
}
