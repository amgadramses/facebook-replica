package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Entities.FriendRequests;
import Entities.Friends;
import Entities.User;
import Redis.UserCache;
import ResourcePools.ArangoDBConnectionPool;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.util.MapBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.logging.Logger;

public class GetFriendsCommand extends Command {
    private final Logger log = Logger.getLogger(GetFriendsCommand.class.getName());
    final String USERS_COLLECTION = "Users";
    final String DB_NAME = "SocialDB";
    final String FRIENDS_COLLECTION = "Friends";
    final String BLOCKS_COLLECTION = "Blocks";
    private String user_id = "";
    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        arangoDB = ArangoDBConnectionPool.getDriver();
        ArangoDatabase db = arangoDB.db(DB_NAME);
        String modified_user_id = USERS_COLLECTION+"/"+user_id;

        String query = "LET Q1 = (FOR block IN "+ BLOCKS_COLLECTION+" FILTER block.`_from` == @value ||block.`_to` == @value RETURN APPEND([], [block.`_from`,block.`_to` ])) LET Q2 = (MINUS(UNIQUE(FLATTEN(Q1)), [@value])) FOR friend IN "+FRIENDS_COLLECTION+ " FILTER (friend.`_to` == @value || friend.`_from` == @value) && !(POSITION(Q2, friend.`_to` ) || POSITION(Q2, friend.`_from`)) RETURN friend";
        //"LET Q1 = (FOR block IN "+ BLOCKS_COLLECTION+" FILTER block.`_from` == @value ||block.`_to` == @value RETURN APPEND([], [block.`_from`,block.`_to` ])) LET Q2 = (MINUS(UNIQUE(FLATTEN(Q1)), [@value])) LET countVal = (FOR friend IN "+FRIENDS_COLLECTION+ " FILTER (friend.`_to` == @value || friend.`_from` == @value) && !(POSITION(Q2, friend.`_to` ) || POSITION(Q2, friend.`_from`)) RETURN friend) RETURN count(countVal)";

        Map<String, Object> bindVars = new MapBuilder().put("value", modified_user_id).get();
        ArangoCursor<BaseEdgeDocument> cursor = db.query(query, bindVars, null, BaseEdgeDocument.class);

        if(cursor.hasNext()) {
            Friends friends = new Friends();
            while (cursor.hasNext()) {
                friends.add(cursor.next());
            }

            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.set("friendRequests", nf.pojoNode(friends));

            try {
                UserCache.userCache.set(parameters.get("method")+":"+user_id, mapper.writeValueAsString(responseJson));
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

    }
        else{
            System.out.println("ELSe");
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("message","You have no friends :(");
            try {
                UserCache.userCache.set(parameters.get("method")+":"+user_id, mapper.writeValueAsString(responseJson));
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
    }
}
