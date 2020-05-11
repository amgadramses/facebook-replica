package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Redis.UserCache;
import ResourcePools.ArangoDBConnectionPool;
import com.arangodb.*;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.util.MapBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AcceptFriendRequestCommand extends Command {
    private final Logger log = Logger.getLogger(AcceptFriendRequestCommand.class.getName());
    final String DB_NAME = "SocialDB";
    String requestID;
    String user_id;
    String requestSenderID;
    final String USERS_COLLECTION = "Users";
    final String FRIENDS_COLLECTION = "Friends";
    final String REQUEST_COLLECTION = "FriendRequests";
    final String DB_GRAPH = "SocialDBGraph";

    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        requestSenderID = parameters.get("requestSenderID");
        requestID = parameters.get("requestID");
        String modified_user_id = USERS_COLLECTION + "/" + user_id;

        arangoDB = ArangoDBConnectionPool.getDriver();
        ArangoDatabase db = arangoDB.db(DB_NAME);
        ArangoGraph graph = db.graph(DB_GRAPH);
        ArangoEdgeCollection friendsCollection = graph.edgeCollection(FRIENDS_COLLECTION);

        String removeQuery = "FOR doc IN " + REQUEST_COLLECTION + " FILTER doc.`_key` == @value REMOVE doc in " + REQUEST_COLLECTION;
        Map<String, Object> bindVars = new MapBuilder().put("value", requestID).get();
        ArangoCursor<BaseEdgeDocument> cursor = db.query(removeQuery, bindVars, null, BaseEdgeDocument.class);

        BaseEdgeDocument friendsRelation = new BaseEdgeDocument(modified_user_id, requestSenderID);
        friendsRelation.setKey(requestID);
        friendsCollection.insertEdge(friendsRelation);

        try {
            UserCache.userCache.del("retrieveFriendRequests" + ":" + user_id);
            UserCache.userCache.del("getFriends" + ":" + user_id);

            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("Message", "You became friends with " + requestSenderID);

            CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);

        } catch (ArangoDBException | JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
