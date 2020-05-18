package NettyHTTP;

import RabbitMQ.Message;
import RabbitMQ.RabbitMQConfig;
import RabbitMQ.Receiver;

import java.util.concurrent.Callable;

public class Notifier implements Callable<String> {

    private HTTPHandler serverHandler;
    private String queueName;
    private String responseBody;

    public Notifier(HTTPHandler serverHandler,
                    String queueName) {
        this.serverHandler = serverHandler;
        this.setQueueName(queueName);
    }


    @Override
    public String call() throws Exception {
        RabbitMQConfig config = new RabbitMQConfig(getQueueName().toUpperCase());
        Receiver r = new Receiver(config, serverHandler.getCorrelationId());

        Message response = r.receive();
//        r.getChannel().close();
        r.getConnection().close();
        return response.getBody();
    }

    public HTTPHandler getServerHandler() {
        return serverHandler;
    }

    public void setServerHandler(HTTPHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }


}
