package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Redis.UserCache;
import ResourcePools.PostgresConnection;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.SQLException;
import java.util.logging.Logger;

public class RemoveWorkCommand extends Command {
    private final Logger log = Logger.getLogger(RemoveWorkCommand.class.getName());

    @Override
    protected void execute() {
        try {
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(true);

            proc = dbConn.prepareCall("{call remove_work(?)}");
            proc.setPoolable(true);

            proc.setInt(1, Integer.parseInt(parameters.get("work_id")));

            proc.execute();
            proc.close();
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("message", "Work removed successfully.");
            UserCache.userCache.del("get_works" + ":" + parameters.get("user_id"));

            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();


        } finally {
            PostgresConnection.disconnect(set, proc, dbConn, null);
        }

    }
}
