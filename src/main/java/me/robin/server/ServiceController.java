package me.robin.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Lubin.Xuan on 2015/10/8.
 * ie.
 */
public class ServiceController {
    private static Logger logger = LoggerFactory.getLogger(ServiceController.class);

    private final int port;

    private final CommandHandler commandHandler;

    private Thread controllerThread;

    private AtomicInteger threadId = new AtomicInteger(0);

    private ServerSocket server;

    public ServiceController(int port, CommandHandler commandHandler) {
        this.port = port;
        this.commandHandler = commandHandler == null ? new CommandHandler.Basehandler() : commandHandler;
        new Thread(new Daemon()).start();
    }

    class Daemon implements Runnable {
        public void run() {
            new Timer("ServerSocketDaemonThread").schedule(new TimerTask() {
                @Override
                public void run() {
                    if (null == controllerThread || !controllerThread.isAlive()) {
                        startController();
                    }
                }
            }, 0, 5000);
        }
    }

    class Client implements Runnable {

        private Socket acceptSocket;

        Client(Socket acceptSocket) {
            this.acceptSocket = acceptSocket;
        }

        @Override
        public void run() {
            try {
                Echo echo = new Echo(acceptSocket);
                echo.echo("welcome to visit server.");
                String command;
                while ((command = echo.readLine()) != null) {
                    command = command.trim();
                    logger.debug("Command Received ---> {}", command);
                    if ("q".equals(command)) {
                        break;
                    } else if ("heart".equals(command)) {
                        echo._echo(200);
                    } else {
                        echo.echo(commandHandler.handle(command));
                    }
                }
                echo.close();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    private void startController() {
        controllerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    server = new ServerSocket(port);
                    while (true) {
                        Socket acceptSocket = server.accept();
                        Thread client = new Thread(new Client(acceptSocket));
                        client.setName("RemoteControllerThread-" + threadId.incrementAndGet());
                        client.start();
                    }
                } catch (Exception e) {
                    logger.error("{}", e.toString());
                    if (null != server) {
                        try {
                            server.close();
                        } catch (Exception ignored) {

                        }
                    }
                }
            }
        });
        controllerThread.setName("Remote Controller Thread");
        controllerThread.start();
    }
}
