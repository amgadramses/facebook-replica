package CommandDesign;

import RabbitMQ.RabbitMQConfig;
import RabbitMQ.Sender;

import java.util.logging.Logger;

public class CommandsHelp {

    public static void submit(String app, String json, String correlationID, Logger logger){
        Sender s = new Sender(new RabbitMQConfig(app.toUpperCase()+".OUTQUEUE"));
        s.send(json, correlationID, logger);
    }
}
