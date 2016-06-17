package me.robin.jms;

/**
 * Created by xuanlubin on 2016/6/8.
 */
public interface JMSHandler {
    public boolean handle(String fileContent);

    public boolean available();
}
