package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Entities.Followers;
import Entities.Following;
import ResourcePools.ArangoDBConnectionPool;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.util.MapBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.logging.Logger;

public class GetFollowingCommand extends Command {
    private final Logger log = Logger.getLogger(GetFollowingCommand.class.getName());
    final String USERS_COLLECTION = "Users";
    final String DB_NAME = "SocialDB";
    final String REQUEST_COLLECTION = "Follows";
    private String user_id = "";

    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        arangoDB = ArangoDBConnectionPool.getDriver();
        ArangoDatabase db = arangoDB.db(DB_NAME);
        String modified_user_id = USERS_COLLECTION+"/"+user_id;
        String query = "FOR doc IN " + REQUEST_COLLECTION + " FILTER doc.`_from` == @value RETURN doc";
        Map<String, Object> bindVars = new MapBuilder().put("value", modified_user_id).get();
        ArangoCursor<BaseEdgeDocument> cursor = db.query(query, bindVars, null, BaseEdgeDocument.class);

        if(cursor.hasNext()){
            Following following = new Following();
            while (cursor.hasNext()) {
                following.add(cursor.next());
            }

            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.set("friendRequests", nf.pojoNode(following));

            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
        else{
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("message","You are not following anyone");
            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
