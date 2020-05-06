package RabbitMQ;

import com.rabbitmq.client.impl.AMQBasicProperties;

public class Message {

    AMQBasicProperties props;
    String body;

    public Message(String body, AMQBasicProperties props){
        this.body = body;
        this.props = props;
    }
    public AMQBasicProperties getProps() {
        return props;
    }

    public String getBody() {
        return body;
    }

}
