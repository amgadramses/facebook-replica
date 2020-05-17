package ResourcePools;

import com.arangodb.ArangoDB;

public class ArangoDBConnectionPool {
    private final static int MAX_CONNECTIONS = 30;
    private static ArangoDB arangoDB;

    public static void initSource() {
        arangoDB = new ArangoDB.Builder().host("arangodb",8529).maxConnections(MAX_CONNECTIONS).build();
    }

    public static ArangoDB getDriver() {
        return arangoDB;
    }
}
