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

public class GetBlockedUsersCommand extends Command {
    private final Logger log = Logger.getLogger(GetBlockedUsersCommand.class.getName());
    final String USERS_COLLECTION = "Users";
    final String DB_NAME = "SocialDB";
    final String REQUEST_COLLECTION = "Blocks";
    private String user_id = "";
    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        arangoDB = ArangoDBConnectionPool.getDriver();
        ArangoDatabase db = arangoDB.db(DB_NAME);
        String modified_user_id = USERS_COLLECTION+"/"+user_id;

        String query = "FOR doc IN "+ REQUEST_COLLECTION +" FILTER doc.`_from` == @value RETURN doc";
        Map<String, Object> bindVars = new MapBuilder().put("value", modified_user_id).get();
        ArangoCursor<BaseEdgeDocument> cursor = db.query(query, bindVars, null, BaseEdgeDocument.class);

        if(cursor.hasNext()) {
            ArrayList<BaseEdgeDocument> blockedUsers = new ArrayList<BaseEdgeDocument>();
            while (cursor.hasNext()) {
                blockedUsers.add(cursor.next());
            }

            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.set("blockedUsers", nf.pojoNode(blockedUsers));

            try {
                UserCache.userCache.set(parameters.get("method")+":"+user_id, mapper.writeValueAsString(responseJson));

                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }

        }
        else{
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("message","You are not blocking anyone");

            try {
                UserCache.userCache.set(parameters.get("method")+":"+user_id, mapper.writeValueAsString(responseJson));

                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }

        }
    }
}
