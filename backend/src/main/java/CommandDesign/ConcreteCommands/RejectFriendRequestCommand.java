package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import com.arangodb.*;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.util.MapBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RejectFriendRequestCommand extends Command {

    private final Logger log = Logger.getLogger(LoginCommand.class.getName());
    final String DB_NAME = "SocialDB";
    String requestID;
    String user_id;
    String requestSenderID;
    final String REQUEST_COLLECTION = "SendRequest";

    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        requestSenderID = parameters.get("requestSenderID");
        requestID = parameters.get("requestID");

        ArangoDB arangoDB = new ArangoDB.Builder().build();
        ArangoDatabase db = arangoDB.db(DB_NAME);

        String removeQuery = "FOR doc IN "+ REQUEST_COLLECTION  +" FILTER doc.`_key` == @value REMOVE doc in SendRequest";
        Map<String, Object> bindVars = new MapBuilder().put("value", requestID).get();
        ArangoCursor<BaseEdgeDocument> cursor = db.query(removeQuery, bindVars, null, BaseEdgeDocument.class);

        try{
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("Message", requestSenderID+"'s Friend request is rejected!");

            CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);

        } catch (ArangoDBException | JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
