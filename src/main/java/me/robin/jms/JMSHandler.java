package me.robin.jms;

/**
 * Created by xuanlubin on 2016/6/8.
 */
public interface JMSHandler<T> {
    public boolean handle(T data);

    public boolean available();
}
