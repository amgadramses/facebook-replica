package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import ResourcePools.PostgresConnection;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Date;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class RegisterCommand extends Command {
    private final Logger log = Logger.getLogger(RegisterCommand.class.getName());


    @Override
    protected void execute() {
        try {
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(true);
            proc = dbConn.prepareCall("{ call register_user(?,?,?,?,?,?) }");
            proc.setPoolable(true);
            String encrypted_password = sha256Hex(parameters.get("password"));
            proc.setString(1, parameters.get("first_name"));
            proc.setString(2, parameters.get("last_name"));
            proc.setString(3, parameters.get("email"));
            proc.setString(4, parameters.get("phone"));
            proc.setDate(5, Date.valueOf(parameters.get("birth_date")));
            proc.setString(6, encrypted_password);
            proc.execute();
            proc.close();
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
