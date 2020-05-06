package RabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver {
    RabbitMQConfig config;
    Logger logger = Logger.getLogger(Receiver.class.getName());
    Channel channel;
    Connection connection;
    String corrID = null;
    String queueName;

    public Receiver(RabbitMQConfig config) {
        this.config = config;
        try {
            connection = config.connect();
            channel = connection.createChannel();
            queueName = config.getQueueName() + ".INQUEUE";
            channel.queueDeclare(queueName, false, false, false, null);
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
            queueName = config.getQueueName() + ".INQUEUE";//SHOULD BE OUTQUEUE
            channel.queueDeclare(queueName, false, false, false, null);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    public String receive() throws IOException, InterruptedException {
        if(corrID == null)
            return channel.basicConsume(queueName, true, null);
        else{
            final BlockingQueue<String> msg = new ArrayBlockingQueue<String>(1);
            channel.basicConsume(queueName, true, new DefaultConsumer(channel) {

                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    System.out.println("HANDLE");
                    if (properties.getCorrelationId().equals(corrID)) {
                        msg.offer(properties.getCorrelationId());
                    }
                }
            });
            System.out.println("return");
            System.out.println("take "+msg.take());
            return msg.take();
    }

    }

    public static void main(String[] args) {
        {
            Receiver r2 = new Receiver(new RabbitMQConfig("USER"),"5f0f0c39-8e09-4cd8-b2c5-0f6eebe0f5c3");
//            Receiver r1 = new Receiver(new RabbitMQConfig("USER"));
            try {
                System.out.println("R2 received: "+ r2.receive());
                System.out.println("TRY");
//                System.out.println("R1 received: "+ r1.receive());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("CATCH");
            }
        }
    }
}
