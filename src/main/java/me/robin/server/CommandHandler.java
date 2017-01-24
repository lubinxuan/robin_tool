package me.robin.server;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2015/10/8.
 * ie.
 */
public class CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private ServiceController controller;

    @Value("${controller.port:55555}")
    private int port;

    private static final Map<String, CommandAction> actionMap = new HashMap<>();
    private static final List<CommandAction> actionList = new ArrayList<>();

    public synchronized void init() {
        if (null == controller) {
            controller = new ServiceController(port, this);
        }
    }


    protected String defaultMsg(String command) {
        return "No Such Command [" + command + "] Please Check";
    }


    public static void register(String command, CommandAction consumer) {
        if (StringUtils.isBlank(command)) {
            actionList.add(consumer);
        } else {
            actionMap.put(command, consumer);
        }
        logger.info("注册指令:{} 描述:{}", command, consumer);
    }

    public static void register(CommandAction consumer) {
        register(null, consumer);
    }

    public Object handle(String command) {
        if (actionMap.containsKey(command)) {
            return actionMap.get(command).process(command);
        } else {
            for (CommandAction action : actionList) {
                if (action.accept(command)) {
                    return action.process(command);
                }
            }
            return defaultMsg(command);
        }
    }


}