package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Redis.UserCache;
import ResourcePools.ArangoDBConnectionPool;
import com.arangodb.*;
import com.arangodb.entity.BaseEdgeDocument;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockOrUnblockCommand extends Command {
    private final Logger log = Logger.getLogger(BlockOrUnblockCommand.class.getName());
    final String USERS_COLLECTION = "Users";
    final String DB_NAME = "SocialDB";
    final String DB_GRAPH = "SocialDBGraph";
    final String BLOCKS_COLLECTION = "Blocks";
    String user_id;
    String blocked_id;

    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        blocked_id = parameters.get("blocked_id");
        String modified_user_id = USERS_COLLECTION+"/"+user_id;
        String modified_blocked_id = USERS_COLLECTION+"/"+blocked_id;
        arangoDB = ArangoDBConnectionPool.getDriver();
        ArangoDatabase db = arangoDB.db(DB_NAME);
        ArangoGraph graph = db.graph(DB_GRAPH);
        ArangoEdgeCollection collection = graph.edgeCollection(BLOCKS_COLLECTION);

        String query = "FOR doc IN "+ BLOCKS_COLLECTION +" FILTER doc.`_from` == " + "\""+ modified_user_id+"\"" +" && doc.`_to` == "+ "\""+ modified_blocked_id+"\"" +" RETURN doc";
        ArangoCursor<BaseEdgeDocument> cursor = db.query(query, null, null, BaseEdgeDocument.class);


        if(!cursor.hasNext()){ //Block
            BaseEdgeDocument blocked = new BaseEdgeDocument(modified_user_id, modified_blocked_id);
            try {
                collection.insertEdge(blocked);
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "ok");
                responseJson.put("code", "200");
                responseJson.put("message", "You have blocked "+blocked_id);
                UserCache.userCache.del("getBlockedUsers" + ":" + parameters.get("user_id"));
                UserCache.userCache.del("getFriends" + ":" + parameters.get("user_id"));
                UserCache.userCache.del("getFollowers" + ":" + parameters.get("user_id"));
                UserCache.userCache.del("getFollowing" + ":" + parameters.get("user_id"));

                //UserCache.userCache.del("getBlockedUsers" + ":" + blocked_id);
                UserCache.userCache.del("getFriends" + ":" + blocked_id);
                UserCache.userCache.del("getFollowers" + ":" + blocked_id);
                UserCache.userCache.del("getFollowing" + ":" + blocked_id);
                UserCache.userCache.del("showProfile"+":"+parameters.get("user_id"));

                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);

            } catch (ArangoDBException | JsonProcessingException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        else{ //Unblock
            String removeQuery = "FOR doc IN "+ BLOCKS_COLLECTION +" FILTER doc.`_from` == "+"\""+ modified_user_id+"\"" +" && doc.`_to` == "+"\""+ modified_blocked_id+"\""+" REMOVE doc in "+BLOCKS_COLLECTION;
            ArangoCursor<BaseEdgeDocument> cursor2 = db.query(removeQuery, null, null, BaseEdgeDocument.class);

            try {
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "ok");
                responseJson.put("code", "200");
                responseJson.put("message", "You have successfully unblocked "+blocked_id);
                UserCache.userCache.del("getBlockedUsers" + ":" + parameters.get("user_id"));
                UserCache.userCache.del("getFriends" + ":" + parameters.get("user_id"));
                UserCache.userCache.del("getFollowers" + ":" + parameters.get("user_id"));
                UserCache.userCache.del("getFollowing" + ":" + parameters.get("user_id"));

                UserCache.userCache.del("getFriends" + ":" + blocked_id);
                UserCache.userCache.del("getFollowers" + ":" + blocked_id);
                UserCache.userCache.del("getFollowing" + ":" + blocked_id);

                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);

            } catch (ArangoDBException | JsonProcessingException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }

        }

    }
}
