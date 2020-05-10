package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import com.arangodb.*;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.model.EdgeCreateOptions;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendFriendRequestCommand extends Command {
    private final Logger log = Logger.getLogger(LoginCommand.class.getName());
    final String DB_NAME = "SocialDB";
    final String USERS_COLLECTION = "Users";
    final String REQUEST_GRAPH = "SendRequestGraph";
    final String REQUEST_COLLECTION = "SendRequest";
    private String senderID = "";
    private String receiverID = "";
    private String requestID;


    @Override
    protected void execute(){
        int receiver = Integer.parseInt(parameters.get("receiver_id"));
        int sender = Integer.parseInt(parameters.get("sender_id"));
        receiverID = USERS_COLLECTION+"/"+ receiver;
        senderID = USERS_COLLECTION+"/"+sender;
        requestID = (int) (sender * receiver + (Math.pow((Math.abs(sender-receiver) - 1),2)/4))+""; // Unordered Pairing Function
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        ArangoDatabase db = arangoDB.db(DB_NAME);
        ArangoGraph graph = db.graph(REQUEST_GRAPH);
        ArangoEdgeCollection collection = graph.edgeCollection(REQUEST_COLLECTION);
        BaseEdgeDocument res = collection.getEdge(requestID, BaseEdgeDocument.class);
        if(res == null) {
            BaseEdgeDocument friendRequest = new BaseEdgeDocument(senderID, receiverID);
            friendRequest.setKey(requestID);

            try {
                collection.insertEdge(friendRequest);
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "ok");
                responseJson.put("code", "200");
                responseJson.put("request_id", requestID);
                responseJson.put("from", senderID);
                responseJson.put("to", receiverID);
                responseJson.put("requestStatus", "pending");
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);

            } catch (ArangoDBException | JsonProcessingException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        else{
            try {
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "Bad Request");
                responseJson.put("code", "400");
                responseJson.put("message", "You can't send this request");
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            }
            catch (JsonProcessingException e){
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
