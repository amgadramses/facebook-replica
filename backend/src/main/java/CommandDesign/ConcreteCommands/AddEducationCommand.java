package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import ResourcePools.PostgresConnection;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Logger;

public class AddEducationCommand extends Command {
    private final Logger log = Logger.getLogger(AddEducationCommand.class.getName());

    @Override
    protected void execute() {
        try{
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(true);
            proc = dbConn.prepareCall("{ call add_education(?,?,?,?,?) }");
            proc.setPoolable(true);
            proc.setInt(1, Integer.parseInt(parameters.get("user_id")));
            proc.setString(2, parameters.get("institution"));
            proc.setDate(3, Date.valueOf(parameters.get("start_date")));
            proc.setString(5, parameters.get("degree"));
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
            responseJson.put("message", "Successfully added a new education record.");

        }
        catch (SQLException e) {
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "Invalid Request");
            responseJson.put("code", "400");
            if(e.getMessage().contains("Education_pk"))
                responseJson.put("message", "Degree & institution already exist.");
            else{
                responseJson.put("message", e.getSQLState());
                e.printStackTrace();
            }
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
