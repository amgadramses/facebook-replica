package RabbitMQ;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sender {


    RabbitMQConfig config;

    public Sender(RabbitMQConfig config) {
        this.config = config;
    }

    public void send(Message message, Logger logger) {
        Connection connection = null;
        try {


            connection = config.connect();
            Channel channel = connection.createChannel();
            channel.queueDeclare(config.getQueueName() + ".INQUEUE", false, false, false, null);
            channel.basicPublish("", config.getQueueName() + ".INQUEUE", (AMQP.BasicProperties) message.getProps(), message.getBody().getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message.getBody() + "'");
            channel.close();
            config.disconnect(connection);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public RabbitMQConfig getConfig() {
        return config;
    }
}