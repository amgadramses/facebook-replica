package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Redis.UserCache;
import ResourcePools.ArangoDBConnectionPool;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.util.MapBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetFollowersCommand extends Command {
    private final Logger log = Logger.getLogger(GetFollowersCommand.class.getName());
    final String USERS_COLLECTION = "Users";
    final String DB_NAME = "SocialDB";
    final String FOLLOWS_COLLECTION = "Follows";
    final String BLOCKS_COLLECTION = "Blocks";
    private String user_id = "";

    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        arangoDB = ArangoDBConnectionPool.getDriver();
        ArangoDatabase db = arangoDB.db(DB_NAME);
        String modified_user_id = USERS_COLLECTION+"/"+user_id;

        String query = "LET Q1 = (FOR block IN "+ BLOCKS_COLLECTION+" FILTER block.`_from` == @value ||block.`_to` == @value RETURN APPEND([], [block.`_from`,block.`_to` ])) LET Q2 = (MINUS(UNIQUE(FLATTEN(Q1)), [@value])) FOR friend IN "+FOLLOWS_COLLECTION+ " FILTER friend.`_to` == @value  && !(POSITION(Q2, friend.`_to` ) || POSITION(Q2, friend.`_from`)) RETURN friend";
        Map<String, Object> bindVars = new MapBuilder().put("value", modified_user_id).get();
        ArangoCursor<BaseEdgeDocument> cursor = db.query(query, bindVars, null, BaseEdgeDocument.class);

        if(cursor.hasNext()){
            ArrayList<BaseEdgeDocument> followers = new ArrayList<BaseEdgeDocument>();
            while (cursor.hasNext()) {
                followers.add(cursor.next());
            }

            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.set("followers", nf.pojoNode(followers));

            try {
                UserCache.userCache.set(parameters.get("method")+":"+user_id, mapper.writeValueAsString(responseJson));
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }

        }
        else{
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("message","You currently have no followers");
            try {
                UserCache.userCache.set(parameters.get("method")+":"+user_id, mapper.writeValueAsString(responseJson));
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}