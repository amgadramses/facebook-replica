package RabbitMQ;

import NettyHTTP.NettyHTTPServer;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver {
    RabbitMQConfig config;
    Logger logger = Logger.getLogger(Receiver.class.getName());
    Channel channel;
    Connection connection;
    String corrID = null;
    String queueName;
    Consumer consumer;
    final BlockingQueue<Message> response = new ArrayBlockingQueue<Message>(1);

    public Receiver(RabbitMQConfig config) {
        this.config = config;
        try {
            connection = config.connect();
            channel = connection.createChannel();
            queueName = config.getQueueName() + ".INQUEUE";
            channel.queueDeclare(queueName, false, false, false, null);
            consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    Message msg = new Message(new String(body, "UTF-8"), properties);
                    response.offer(msg);
                    }
            };
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public Receiver(RabbitMQConfig config, String correlationID) {
        this.config = config;
        corrID = correlationID;
        try {
            connection = config.connect();
            channel = connection.createChannel();
            queueName = config.getQueueName() + ".OUTQUEUE";//SHOULD BE OUTQUEUE
            channel.queueDeclare(queueName, false, false, false, null);
            consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrID)) {
                        Message msg = new Message(new String(body, "UTF-8"), properties);

                        response.offer(msg);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } else {
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                    }
                }
            };
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public Message receive() throws IOException, InterruptedException, TimeoutException {
        if (corrID == null)
            channel.basicConsume(queueName, true, consumer);
        else
            channel.basicConsume(queueName, false, consumer);
        return response.take();
    }

    public Channel getChannel() {
        return channel;
    }

    public Connection getConnection() {
        return connection;
    }

}
