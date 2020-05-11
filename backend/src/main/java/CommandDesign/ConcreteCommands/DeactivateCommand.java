package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import ResourcePools.PostgresConnection;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.*;
import java.util.logging.Logger;

public class DeactivateCommand extends Command {
    private final Logger log = Logger.getLogger(DeactivateCommand.class.getName());

    protected void execute() {
        try {
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(true);

            proc = dbConn.prepareCall("{? = call deactivate(?)}");
            proc.registerOutParameter(1, Types.BOOLEAN);
            proc.setPoolable(true);

            proc.setInt(2, Integer.parseInt(parameters.get("user_id")));

            proc.execute();
            boolean is_active = proc.getBoolean(1);

            proc.close();
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            String msg = (is_active)?"Your account was successfully re-activated.":"Your account is successfully deactivated.";
            responseJson.put("message", msg);

            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PostgresConnection.disconnect(set, proc, dbConn, null);
        }
    }

}
