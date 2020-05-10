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

public class AcceptFriendRequestCommand extends Command {
    private final Logger log = Logger.getLogger(LoginCommand.class.getName());
    final String DB_NAME = "SocialDB";
    String requestID;
    String user_id;
    String requestSenderID;
    final String FRIENDS_COLLECTION = "Friends";
    final String REQUEST_COLLECTION = "SendRequest";
    final String REQUEST_GRAPH = "FriendsGraph";

    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        requestSenderID = parameters.get("requestSenderID");
        requestID = parameters.get("requestID");

        ArangoDB arangoDB = new ArangoDB.Builder().build();
        ArangoDatabase db = arangoDB.db(DB_NAME);
        ArangoGraph graph = db.graph(REQUEST_GRAPH);
        ArangoEdgeCollection friendsCollection = graph.edgeCollection(FRIENDS_COLLECTION);

        String removeQuery = "FOR doc IN "+ REQUEST_COLLECTION  +" FILTER doc.`_key` == @value REMOVE doc in SendRequest";
        Map<String, Object> bindVars = new MapBuilder().put("value", requestID).get();
        ArangoCursor<BaseEdgeDocument> cursor = db.query(removeQuery, bindVars, null, BaseEdgeDocument.class);

        BaseEdgeDocument friendsRelation = new BaseEdgeDocument("Users/"+user_id, requestSenderID);
        friendsRelation.setKey(requestID);
        friendsCollection.insertEdge(friendsRelation);

        try{
        responseJson.put("app", parameters.get("app"));
        responseJson.put("method", parameters.get("method"));
        responseJson.put("status", "ok");
        responseJson.put("code", "200");
        responseJson.put("Message", "You became friends with "+ requestSenderID);

        CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);

    } catch (ArangoDBException | JsonProcessingException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
    }
    }
}
