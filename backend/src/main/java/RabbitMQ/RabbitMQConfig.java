package RabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConfig {

    private Connection connection;
    private String queueName;

    public RabbitMQConfig(String queueName) {
        this.queueName = queueName;
    }

    public Connection connect() throws IOException, TimeoutException {
        if (connection == null) {
//            connection = connectionFactory.createConnection();
//            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
//            connection.start();
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq");
            connection = factory.newConnection();

        }
        return connection;
    }

    public void disconnect(Connection connection) throws IOException {
        connection.close();
    }

    public String getQueueName() {
        return this.queueName;
    }
}