package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import ResourcePools.ArangoDBConnectionPool;
import ResourcePools.PostgresConnection;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Logger;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class RegisterCommand extends Command {
    private final Logger log = Logger.getLogger(RegisterCommand.class.getName());
    final String DB_NAME = "SocialDB";
    final String USERS_COLLECTION = "Users";


    @Override
    protected void execute() {
        try {
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(true);
            proc = dbConn.prepareCall("{ ? = call register_user(?,?,?,?,?,?) }");
            proc.setPoolable(true);
            proc.registerOutParameter(1, Types.INTEGER);
            String encrypted_password = sha256Hex(parameters.get("password"));
            proc.setString(2, parameters.get("first_name"));
            proc.setString(3, parameters.get("last_name"));
            proc.setString(4, parameters.get("email").toLowerCase());
            proc.setString(5, parameters.get("phone"));
            proc.setDate(6, Date.valueOf(parameters.get("birth_date")));
            proc.setString(7, encrypted_password);
            proc.execute();
            int user_id = proc.getInt(1);
            proc.close();
            BaseDocument myObject = new BaseDocument();
            myObject.setKey(String.valueOf(user_id));
            myObject.addAttribute("first_name", parameters.get("first_name"));
            myObject.addAttribute("last_name", parameters.get("last_name"));
            try {
                arangoDB = ArangoDBConnectionPool.getDriver();
                ArangoDatabase db = arangoDB.db(DB_NAME);
                db.collection(USERS_COLLECTION).insertDocument(myObject);
                System.out.println("Document created");
            } catch (ArangoDBException e) {
                System.err.println("Failed to create document. " + e.getMessage());
            }


            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("message", "Successfully signed up.");
            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "Invalid Request");
            responseJson.put("code", "400");
            if(e.getMessage().contains("Unique_email"))
                responseJson.put("message", "Email already exists.");
            else
                responseJson.put("message", e.getSQLState());
            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }
        } finally {
            PostgresConnection.disconnect(set, proc, dbConn, null);
        }
    }
}
