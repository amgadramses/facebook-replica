package RabbitMQ;

import NettyHTTP.NettyHTTPServer;
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
            queueName = config.getQueueName() + ".OUTQUEUE";//SHOULD BE OUTQUEUE
            channel.queueDeclare(queueName, false, false, false, null);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    public Message receive() throws IOException, InterruptedException {
        final BlockingQueue<Message> response = new ArrayBlockingQueue<Message>(1);

        if(corrID == null)
            channel.basicConsume(queueName, true, new DefaultConsumer(channel) {

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    Message msg = new Message(new String(body, "UTF-8"), properties);
                    response.offer(msg);
                }
            });

        else{

            channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrID)) {
                        Message msg = new Message(new String(body, "UTF-8"), properties);
                        response.offer(msg);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    }
                    else{
                        channel.basicNack(envelope.getDeliveryTag(),false, true);
                    }
                }
            });
    }
        return response.take();

    }

    public Channel getChannel() {
        return channel;
    }

    public Connection getConnection() {
        return connection;
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        {
            Message msg = null;
//            Receiver r2 = new Receiver(new RabbitMQConfig("USER"),"d037e384-c718-4c84-b4df-317b3b9a5531");
            Receiver r1 = new Receiver(new RabbitMQConfig("USER"));
            try {
                System.out.println("TRY");
//                System.out.println("R2 received: "+ r2.receive());
                msg = r1.receive();
                System.out.println("R1 received: " + msg);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("CATCH");
            } finally {
                System.out.println("FINAALLLYY");
//              r1.channel.close();
                r1.connection.close();
            }

//            Sender2 s = new Sender2(new RabbitMQConfig("USER"));
//            Logger log = Logger.getLogger(NettyHTTPServer.class.getName());
//
//            s.send("YOUKAAAA",msg.getProps().getCorrelationId(), log);
        }

    }
}
