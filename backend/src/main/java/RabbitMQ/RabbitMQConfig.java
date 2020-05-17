package RabbitMQ;

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
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq"); //should be localhost when you mvn package for the webservers
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