package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Entities.FriendRequests;
import com.arangodb.*;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.util.MapBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class RetrieveFriendRequestsCommand extends Command {
    private final Logger log = Logger.getLogger(LoginCommand.class.getName());
    final String DB_NAME = "SocialDB";
    final String REQUEST_COLLECTION = "SendRequest";
    private String user_id = "";
    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        ArangoDatabase db = arangoDB.db(DB_NAME);

        String query = "FOR doc IN "+ REQUEST_COLLECTION +" FILTER doc.`_to` == @value RETURN doc";
        Map<String, Object> bindVars = new MapBuilder().put("value", "Users/"+user_id).get();
        ArangoCursor<BaseEdgeDocument> cursor = db.query(query, bindVars, null, BaseEdgeDocument.class);

        if(cursor.hasNext()) {
            FriendRequests requests = new FriendRequests();
            while (cursor.hasNext()) {
                requests.add(cursor.next());
            }

            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.set("friendRequests", nf.pojoNode(requests));

            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        else{
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("message","There are no new friend requests");
            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
    }
}
