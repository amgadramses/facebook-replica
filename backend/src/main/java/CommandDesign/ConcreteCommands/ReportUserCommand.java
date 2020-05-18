package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import ResourcePools.ArangoDBConnectionPool;
import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.ArangoEdgeCollection;
import com.arangodb.entity.BaseDocument;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportUserCommand extends Command {
    private final Logger log = Logger.getLogger(ReportUserCommand.class.getName());
    private String user_id;
    private String reported_id;
    private String reason;
    private String comments;
    final String DB_NAME = "SocialDB";
    final String REPORTS_COLLECTION = "Reports";

    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        reported_id = parameters.get("reported_id");
        reason = parameters.get("reason");
        comments = parameters.get("comments");
        String report_id = UUID.randomUUID().toString();
        arangoDB = ArangoDBConnectionPool.getDriver();
        ArangoDatabase db = arangoDB.db(DB_NAME);
        ArangoCollection collection = db.collection(REPORTS_COLLECTION);

        BaseDocument report = new BaseDocument();
        report.setKey(report_id);
        report.addAttribute("reporter_id", user_id);
        report.addAttribute("reported_id", reported_id);
        report.addAttribute("reason", reason);
        report.addAttribute("comments", comments);
        report.addAttribute("report_id", report_id);
        try {
        collection.insertDocument(report);
        responseJson.put("app", parameters.get("app"));
        responseJson.put("method", parameters.get("method"));
        responseJson.put("status", "ok");
        responseJson.put("code", "200");
        responseJson.put("message", "You have successfully reported "+ reported_id);

            CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }

    }
}
