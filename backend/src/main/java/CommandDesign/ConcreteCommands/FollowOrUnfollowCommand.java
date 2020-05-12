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

public class FollowOrUnfollowCommand extends Command {
    private final Logger log = Logger.getLogger(FollowOrUnfollowCommand.class.getName());
    final String USERS_COLLECTION = "Users";
    final String DB_NAME = "SocialDB";
    final String REQUEST_GRAPH = "SocialDBGraph";
    final String FOLLOWS_COLLECTION = "Follows";
    String user_id;
    String followed_id;

    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        followed_id = parameters.get("followed_id");
        String modified_user_id = USERS_COLLECTION+"/"+user_id;
        String modified_followed_id = USERS_COLLECTION+"/"+followed_id;
        arangoDB = ArangoDBConnectionPool.getDriver();
        ArangoDatabase db = arangoDB.db(DB_NAME);
        ArangoGraph graph = db.graph(REQUEST_GRAPH);
        ArangoEdgeCollection collection = graph.edgeCollection(FOLLOWS_COLLECTION);
        System.out.println("FOR doc IN "+ FOLLOWS_COLLECTION +" FILTER doc.`_from` == " + "\""+ modified_user_id+"\"" +" && doc.`_to` == "+ "\""+ modified_followed_id+"\"" +" RETURN doc");

        String query = "FOR doc IN "+ FOLLOWS_COLLECTION +" FILTER doc.`_from` == " + "\""+ modified_user_id+"\"" +" && doc.`_to` == "+ "\""+ modified_followed_id+"\"" +" RETURN doc";
        ArangoCursor<BaseEdgeDocument> cursor = db.query(query, null, null, BaseEdgeDocument.class);

        System.out.println(cursor.hasNext());

        if(!cursor.hasNext()){ //follow
            BaseEdgeDocument followed = new BaseEdgeDocument(modified_user_id, modified_followed_id);
            try {
                collection.insertEdge(followed);
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "ok");
                responseJson.put("code", "200");
                responseJson.put("message", "You are now following "+followed_id);
                UserCache.userCache.del("getFollowing" + ":" + parameters.get("user_id"));
                UserCache.userCache.del("getFollowers" + ":" + followed_id);

                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);

            } catch (ArangoDBException | JsonProcessingException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        else{ //unfollow
            String removeQuery = "FOR doc IN "+ FOLLOWS_COLLECTION +" FILTER doc.`_from` == "+"\""+ modified_user_id+"\"" +" && doc.`_to` == "+"\""+ modified_followed_id+"\""+" REMOVE doc in "+FOLLOWS_COLLECTION;
            ArangoCursor<BaseEdgeDocument> cursor2 = db.query(removeQuery, null, null, BaseEdgeDocument.class);

            try {
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "ok");
                responseJson.put("code", "200");
                responseJson.put("message", "You have successfully unfollowed "+followed_id);
                UserCache.userCache.del("getFollowing" + ":" + parameters.get("user_id"));
                UserCache.userCache.del("getFollowers" + ":" + followed_id);

                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);

            } catch (ArangoDBException | JsonProcessingException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }

        }

    }
}
