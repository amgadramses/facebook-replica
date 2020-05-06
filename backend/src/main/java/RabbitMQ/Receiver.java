package RabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
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

            channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrID)) {
                        msg.offer(properties.getCorrelationId());
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    }
                    else{
                        channel.basicNack(envelope.getDeliveryTag(),false, true);
                    }
                }
            });
            return msg.take();
    }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        {
            Receiver r2 = new Receiver(new RabbitMQConfig("USER"),"37485e0e-aebf-4505-bc29-4fe929729cd8");
//            Receiver r1 = new Receiver(new RabbitMQConfig("USER"));
            try {
                System.out.println("TRY");
                System.out.println("R2 received: "+ r2.receive());
//                System.out.println("R1 received: "+ r1.receive());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("CATCH");
            }
            finally {
                System.out.println("FINAALLLYY");
//                r2.channel.close();
//                r2.connection.close();
            }
        }
    }
}
