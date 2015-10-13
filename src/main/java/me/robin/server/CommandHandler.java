package me.robin.server;

/**
 * Created by Lubin.Xuan on 2015/10/8.
 * ie.
 */
public abstract class CommandHandler {
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