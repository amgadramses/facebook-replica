package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Entities.Education;
import Entities.User;
import Entities.Work;
import Redis.UserCache;
import ResourcePools.ArangoDBConnectionPool;
import ResourcePools.PostgresConnection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.util.MapBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.sql.*;
import java.util.Map;
import java.util.logging.Logger;

public class ShowProfile extends Command {
    //Should return user data + list of education & work + Profile & Cover photo + Friends count + Followers count
    private final Logger log = Logger.getLogger(ShowProfile.class.getName());
    int user_id=-1;
    private String last_name, first_name, email, phone;
    private Timestamp created_at;
    private boolean is_active;
    private Date birth_date;

    final String USERS_COLLECTION = "Users";
    final String DB_NAME = "SocialDB";
    final String FRIENDS_COLLECTION = "Friends";
    final String BLOCKS_COLLECTION = "Blocks";
    final String FOLLOWS_COLLECTION = "Follows";

    @Override
    protected void execute() {
        try {
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(false);
            proc = dbConn.prepareCall("{? = call show_profile(?)}");
            proc.setPoolable(true);
            proc.registerOutParameter(1, Types.OTHER);
            proc.setInt(2, Integer.parseInt(parameters.get("user_id")));
            proc.execute();
            set = (ResultSet) proc.getObject(1);

            ArrayNode works = nf.arrayNode();
            ArrayNode educations = nf.arrayNode();
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");

            User user = new User();

            while (set.next()) {
                if (set.isFirst()) {
                    user_id = set.getInt("user_id");
                    email = set.getString("email");
                    first_name = set.getString("first_name");
                    last_name = set.getString("last_name");
                    created_at = set.getTimestamp("created_at");
                    is_active = set.getBoolean("is_active");
                    phone = set.getString("phone");
                    birth_date = set.getDate("birth_date");
                }

                if (set.getInt("type") == 1) {
                    Education e = new Education();
                    e.setId(set.getInt("id"));
                    e.setStart_date(set.getDate("start_date"));
                    e.setEnd_date(set.getDate("end_date"));
                    e.setInstitution(set.getString("institution"));
                    e.setDegree(set.getString("degree"));
                    educations.addPOJO(e);
                } else {
                    Work e = new Work();
                    e.setId(set.getInt("id"));
                    e.setStart_date(set.getDate("start_date"));
                    e.setEnd_date(set.getDate("end_date"));
                    e.setInstitution(set.getString("institution"));
                    e.setJob_title(set.getString("degree"));
                    works.addPOJO(e);
                }
            }
            if(user_id!=-1) {
                user.setUser_id(user_id);
                user.setEmail(email);
                user.setFirst_name(first_name);
                user.setLast_name(last_name);
                user.setCreated_at(created_at);
                user.setIs_active(is_active);
                user.setPhone(phone);
                user.setBirth_date(birth_date);
                set.close();
                proc.close();

                //TODO NoSQL queries + photos
                arangoDB = ArangoDBConnectionPool.getDriver();
                ArangoDatabase db = arangoDB.db(DB_NAME);
                String modified_user_id = USERS_COLLECTION + "/" + user_id;
                String query = "LET Q1 = (FOR block IN " + BLOCKS_COLLECTION + " FILTER block.`_from` == @value ||block.`_to` == @value RETURN APPEND([], [block.`_from`,block.`_to` ])) LET Q2 = (MINUS(UNIQUE(FLATTEN(Q1)), [@value])) FOR friend IN " + FRIENDS_COLLECTION + " FILTER (friend.`_to` == @value || friend.`_from` == @value) && !(POSITION(Q2, friend.`_to` ) || POSITION(Q2, friend.`_from`)) RETURN friend";
                Map<String, Object> bindVars = new MapBuilder().put("value", modified_user_id).get();
                ArangoCursor<BaseEdgeDocument> cursor = db.query(query, bindVars, new AqlQueryOptions().count(true), BaseEdgeDocument.class);
                int friendsCount = cursor.getCount();
                query = "LET Q1 = (FOR block IN " + BLOCKS_COLLECTION + " FILTER block.`_from` == @value ||block.`_to` == @value RETURN APPEND([], [block.`_from`,block.`_to` ])) LET Q2 = (MINUS(UNIQUE(FLATTEN(Q1)), [@value])) FOR friend IN " + FOLLOWS_COLLECTION + " FILTER friend.`_to` == @value  && !(POSITION(Q2, friend.`_to` ) || POSITION(Q2, friend.`_from`)) RETURN friend";
                cursor = db.query(query, bindVars, new AqlQueryOptions().count(true), BaseEdgeDocument.class);
                int followersCount = cursor.getCount();
                query = "LET Q1 = (FOR block IN " + BLOCKS_COLLECTION + " FILTER block.`_from` == @value ||block.`_to` == @value RETURN APPEND([], [block.`_from`,block.`_to` ])) LET Q2 = (MINUS(UNIQUE(FLATTEN(Q1)), [@value])) FOR friend IN " + FOLLOWS_COLLECTION + " FILTER friend.`_from` == @value  && !(POSITION(Q2, friend.`_to` ) || POSITION(Q2, friend.`_from`)) RETURN friend";
                bindVars = new MapBuilder().put("value", modified_user_id).get();
                cursor = db.query(query, bindVars, new AqlQueryOptions().count(true), BaseEdgeDocument.class);
                int followingCount = cursor.getCount();


                //response
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "ok");
                responseJson.put("code", "200");
                responseJson.set("Educations", educations);
                responseJson.set("Works", works);
                responseJson.set("user", nf.pojoNode(user));
                responseJson.put("FriendsCount", friendsCount);
                responseJson.put("FollowersCount", followersCount);
                responseJson.put("FollowingCount", followingCount);


                try {
                    UserCache.userCache.set(parameters.get("method") + ":" + user_id, mapper.writeValueAsString(responseJson));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            else{
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "ok");
                responseJson.put("code", "200");
                responseJson.put("message", "There's no user having this user ID.");
            }
        } catch (SQLException e) {
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "Bad Request");
            responseJson.put("code", "400");
            responseJson.put("message", "Bad Request");
            e.printStackTrace();
        }
        finally {
            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                PostgresConnection.disconnect(set, proc, dbConn, null);
            }
        }
    }
}
