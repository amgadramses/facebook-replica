package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Redis.UserCache;
import ResourcePools.PostgresConnection;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.SQLException;
import java.util.logging.Logger;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class EditUserCommand extends Command {
    private final Logger log = Logger.getLogger(EditUserCommand.class.getName());
    private boolean change_password = false;

    @Override
    protected void execute() {
        try {

            System.out.println(UserCache.userCache.get("user"));
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(true);
            proc = dbConn.prepareCall("{ call edit_user_detail(?,?,?) }");
            proc.setPoolable(true);
            proc.setInt(1, Integer.parseInt(parameters.get("user_id")));
            proc.setString(2, parameters.get("field"));
            if (parameters.get("field").equals("password")) {
                change_password = true;
                proc.setString(3, sha256Hex(parameters.get("value")));
            } else
                proc.setString(3, parameters.get("value"));

            proc.execute();
            proc.close();
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            if (change_password)
                responseJson.put("message", "Your password has been changed successfully.");
            else
                responseJson.put("message", "Your " + parameters.get("field") + " changed to " + parameters.get("value"));

        } catch (SQLException e) {
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "Invalid Request");
            responseJson.put("code", "400");
            if (e.getMessage().contains("Unique_email"))
                responseJson.put("message", "Email already exists.");
            else
                responseJson.put("message", e.getSQLState());
        } finally {
            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            PostgresConnection.disconnect(set, proc, dbConn, null);

        }
    }
}
