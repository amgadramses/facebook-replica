package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Redis.UserCache;
import ResourcePools.PostgresConnection;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Logger;

public class AddWorkCommand extends Command {
    private final Logger log = Logger.getLogger(AddWorkCommand.class.getName());

    @Override
    protected void execute() {
        try{
        dbConn = PostgresConnection.getDataSource().getConnection();
        dbConn.setAutoCommit(true);
        proc = dbConn.prepareCall("{ call add_work(?,?,?,?,?) }");
        proc.setPoolable(true);
        proc.setInt(1, Integer.parseInt(parameters.get("user_id")));
        proc.setString(2, parameters.get("institution"));
        proc.setDate(3, Date.valueOf(parameters.get("start_date")));
        proc.setString(5, parameters.get("job_title"));
        if(parameters.containsKey("end_date"))
            proc.setDate(4, Date.valueOf(parameters.get("end_date")));
        else
            proc.setNull(4, Types.DATE);
            proc.execute();
            proc.close();
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");
            responseJson.put("message", "Successfully added a new work record.");
            UserCache.userCache.del("get_works" + ":" + parameters.get("user_id"));
            UserCache.userCache.del("showProfile"+":"+parameters.get("user_id"));

        }
        catch (SQLException e) {
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "Invalid Request");
            responseJson.put("code", "400");
            if(e.getMessage().contains("Work_pk"))
                responseJson.put("message", "Job & institution already exist.");
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
