package CommandDesign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;

public abstract class Command implements Runnable {

    public Map<String, String> getParameters() {
        return parameters;
    }

    protected Map<String, String> parameters;
    protected Connection dbConn;
    protected CallableStatement proc;
    protected ResultSet set;
    protected JsonNodeFactory nf = JsonNodeFactory.instance;
    protected ObjectNode responseJson = nf.objectNode();
//    protected Map<String, String> details = new HashMap<String, String>();
    protected MyObjectMapper mapper = new MyObjectMapper();
//    protected boolean shouldReturnResponse() {
//        return true;
//    }

    final public void run() {
        // Some Method
        execute();
    }

    final public void init(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    protected abstract void execute();
}
