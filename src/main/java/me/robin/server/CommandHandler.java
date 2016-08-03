package me.robin.server;

import org.springframework.beans.factory.annotation.Value;

/**
 * Created by Lubin.Xuan on 2015/10/8.
 * ie.
 */
public abstract class CommandHandler {

    private ServiceController controller;

    @Value("${controller.port:55555}")
    private int port;

    public synchronized void init() {
        if (null == controller) {
            controller = new ServiceController(port, this);
        }
    }

    public abstract Object handle(String command);

    protected String defaultMsg(String command) {
        return "No Such Command [" + command + "] Please Check";
    }

    protected static class Basehandler extends CommandHandler {
        @Override
        public Object handle(String command) {
            return defaultMsg(command);
        }
    }
}