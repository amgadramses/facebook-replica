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

    public void send(String msg, String correlationID, Logger logger) {
        Connection connection = null;
        try {

            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(correlationID)
                    .replyTo(config.getQueueName() + ".OUTQUEUE")
                    .build();
            connection = config.connect();
            Channel channel = connection.createChannel();
            channel.queueDeclare(config.getQueueName() + ".INQUEUE", false, false, false, null);
            channel.basicPublish("", config.getQueueName() + ".INQUEUE", props, msg.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + msg + "'");
            channel.close();
            config.disconnect(connection);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }


}