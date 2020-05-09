package RabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Send {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
//        factory.setHost("192.168.1.103");
//        factory.setPort(5672);
        //factory.setUri("amqp://scalable:1@192.168.1.103:5672/scalable_host");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = "Hello Worldjjjjj!";
        int i = 0;
        while(i<5) {
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");
            i++;
        }
        channel.close();
        connection.close();
    }
}