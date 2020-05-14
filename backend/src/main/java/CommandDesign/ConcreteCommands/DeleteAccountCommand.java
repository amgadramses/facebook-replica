package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import Entities.Friends;
import Redis.UserCache;
import ResourcePools.ArangoDBConnectionPool;
import ResourcePools.PostgresConnection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.util.MapBuilder;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

public class DeleteAccountCommand extends Command {
    private final Logger log = Logger.getLogger(DeleteAccountCommand.class.getName());
    final String USERS_COLLECTION = "Users";
    final String DB_NAME = "SocialDB";
    final String FRIENDS_COLLECTION = "Friends";
    final String BLOCKS_COLLECTION = "Blocks";
    final String REQUEST_COLLECTION = "FriendRequests";
    final String FOLLOWS_COLLECTION = "Follows";

    @Override
    protected void execute() {
        try {
            int user_id = Integer.parseInt(parameters.get("user_id"));
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(true);

            proc = dbConn.prepareCall("{call delete_account(?)}");
            proc.setPoolable(true);

            proc.setInt(1, user_id);
            proc.execute();
            proc.close();

            UserCache.userCache.del("showProfile:" + user_id);
            UserCache.userCache.del("getBlockedUsers:" + user_id);
            UserCache.userCache.del("getFollowing:" + user_id);
            UserCache.userCache.del("get_educations:" + user_id);
            UserCache.userCache.del("getFollowers:" + user_id);
            UserCache.userCache.del("getFriends:" + user_id);
            UserCache.userCache.del("get_works:" + user_id);
            UserCache.userCache.del("retrieveFriendRequests:" + user_id);
            UserCache.userCache.del("showProfile" + ":" + parameters.get("user_id"));

            //TODO NoSQL queries and delete photos
            arangoDB = ArangoDBConnectionPool.getDriver();
            ArangoDatabase db = arangoDB.db(DB_NAME);
            String modified_user_id = USERS_COLLECTION+"/"+user_id;
            String query = "REMOVE { `_key`: " + "\""+user_id+"\""+ "} IN Users";
            ArangoCursor<BaseEdgeDocument> cursor = db.query(query, null, null, BaseEdgeDocument.class);
            String friendsRmQuery = "FOR fr IN "+ FRIENDS_COLLECTION+" FILTER fr.`_to` == @value || fr.`_from` == @value REMOVE {`_key`: fr.`_key`} IN "+ FRIENDS_COLLECTION;
            Map<String, Object> bindVars = new MapBuilder().put("value", "Users/"+user_id).get();
            cursor = db.query(friendsRmQuery, bindVars, null, BaseEdgeDocument.class);
            String blocksRmQuery = "FOR fr IN "+ BLOCKS_COLLECTION+" FILTER fr.`_to` == @value || fr.`_from` == @value REMOVE {`_key`: fr.`_key`} IN "+ BLOCKS_COLLECTION;
            cursor = db.query(blocksRmQuery, bindVars, null, BaseEdgeDocument.class);
            String requestsRMQuery = "FOR fr IN "+ REQUEST_COLLECTION+" FILTER fr.`_to` == @value || fr.`_from` == @value REMOVE {`_key`: fr.`_key`} IN "+ REQUEST_COLLECTION;
            cursor = db.query(requestsRMQuery, bindVars, null, BaseEdgeDocument.class);
            String followsRMQuery = "FOR fr IN "+ FOLLOWS_COLLECTION+" FILTER fr.`_to` == @value || fr.`_from` == @value REMOVE {`_key`: fr.`_key`} IN "+ FOLLOWS_COLLECTION;
            cursor = db.query(followsRMQuery, bindVars, null, BaseEdgeDocument.class);


            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("code", "200");
            responseJson.put("message","Your account has been deleted successfully.");


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //TODO send response
            PostgresConnection.disconnect(set, proc, dbConn, null);

        }
    }
}
