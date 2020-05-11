package CommandDesign;

import com.arangodb.ArangoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public abstract class Command implements Runnable {

    public Map<String, String> getParameters() {
        return parameters;
    }

    protected Map<String, String> parameters;
    protected Connection dbConn;
    protected ArangoDB arangoDB;
    protected CallableStatement proc;
    protected ResultSet set;

    protected JsonNodeFactory nf = JsonNodeFactory.instance;
    protected ObjectNode responseJson = nf.objectNode();
    protected Map<String, Object> toBeCached = new HashMap<String, Object>();
    protected MyObjectMapper mapper = new MyObjectMapper();


    final public void run() {
        // Some Method
        execute();
    }
    public static Map<String, Object> ObjectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try { map.put(field.getName(), field.get(obj)); } catch (Exception e) { }
        }
        return map;
    }
    final public void init(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    protected abstract void execute();
}
