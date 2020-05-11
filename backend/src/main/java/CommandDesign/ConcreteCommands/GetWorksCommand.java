package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Entities.Work;
import ResourcePools.PostgresConnection;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetWorksCommand extends Command {
    private final Logger log = Logger.getLogger(GetWorksCommand.class.getName());

    @Override
    protected void execute() {
        try {
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(false);
            proc = dbConn.prepareCall("{? = call get_work(?)}");
            proc.setPoolable(true);
            proc.registerOutParameter(1, Types.OTHER);
            proc.setInt(2, Integer.parseInt(parameters.get("user_id")));
            proc.execute();

            set = (ResultSet) proc.getObject(1);
            ArrayNode works = nf.arrayNode();
            responseJson.put("app", parameters.get("app"));
            responseJson.put("method", parameters.get("method"));
            responseJson.put("status", "ok");
            responseJson.put("code", "200");

            while (set.next()) {
                Work e = new Work();
                e.setId(set.getInt("id"));
                e.setStart_date(set.getDate("start_date"));
                e.setEnd_date(set.getDate("end_date"));
                e.setInstitution(set.getString("institution"));
                e.setJob_title(set.getString("job_title"));
                works.addPOJO(e);
            }
            set.close();
            proc.close();
            responseJson.set("Works", works);
            try {
                CommandsHelp.submit(parameters.get("app"),
                        mapper.writeValueAsString(responseJson),
                        parameters.get("correlation_id"), log);
            } catch (Exception e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }

            dbConn.commit();
        }
        catch(SQLException e){
            log.log(Level.SEVERE, e.getMessage(), e);

        }
        finally {
            PostgresConnection.disconnect(set, proc, dbConn, null);
        }
    }
}
