package Microservices;

import CommandDesign.CommandsMap;
import RabbitMQ.Message;
import RabbitMQ.RabbitMQConfig;
import RabbitMQ.Receiver;
import Redis.UserCache;
import ResourcePools.ArangoDBConnectionPool;
import ResourcePools.JedisConnectionPool;
import ResourcePools.PostgresConnection;
import ResourcePools.WorkerPool;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserMain extends RunnableClasses {
    private static final Logger log = Logger.getLogger(UserMain.class.getName());
    private static WorkerPool pool = new WorkerPool();
    private static boolean run = true;

    public static void main(String[] args) {
        System.out.println("User application is running");
        //PostgresConnection.initSource();
        ArangoDBConnectionPool.initSource();
        JedisConnectionPool.initSource();
        CommandsMap.instantiate();
        UserCache.userBgSave();
        Receiver c = new Receiver(new RabbitMQConfig("USER"));
        try {
            while (run) {
                Message msg = c.receive();
                handleMsg(msg.getBody(), msg.getProps().getCorrelationId(), "user", log, pool);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void shutdown() {
        run = false;
    }

    public static void start() {
        run = true;
    }

}
