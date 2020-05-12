package ResourcePools;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostgresConnection {
    private static final Logger LOGGER = Logger
            .getLogger(PostgresConnection.class.getName());
    private static String DB_USERNAME;   //your db username
    private static String DB_PASSWORD; //your db password
    private static String DB_PORT;
    private static String DB_HOST;
    private static String DB_NAME;
    private static String DB_URL;

    private static final String DB_INIT_CONNECTIONS = "10";
    private static final String DB_MAX_CONNECTIONS = "15";
    private static PoolingDriver dbDriver;
    private static PoolingDataSource<PoolableConnection> dataSource;

    public static void disconnect(ResultSet rs, PreparedStatement statement,
                                  Connection conn, Statement query) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }

        if (query != null) {
            try {
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void disconnect(ResultSet rs, PreparedStatement statement,
                                  Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }


    }

    public static void initSource() {
        try {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE,
                        "Error loading Postgres driver: " + ex.getMessage(), ex);
            }

            try {
                URI dbUri = new URI(System.getenv("DATABASE_URL"));
                DB_USERNAME = dbUri.getUserInfo().split(":")[0];
                DB_PASSWORD = dbUri.getUserInfo().split(":")[1];
                DB_NAME = dbUri.getPath().replace("/", "");
                DB_URL = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

            } catch (Exception e1) {
                try {
//                    readConfFile();
                    readJsonConfFile();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                System.out.println("Used Config File For DB");
            }


            Properties props = new Properties();
            //  System.out.println(DB_USERNAME);
            props.setProperty("user", DB_USERNAME);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("initialSize", DB_INIT_CONNECTIONS);
            props.setProperty("maxActive", DB_MAX_CONNECTIONS);

            DriverManagerConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                    DB_URL, props);
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                    connectionFactory, null);
            poolableConnectionFactory.setPoolStatements(true);

            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxIdle(Integer.parseInt(DB_INIT_CONNECTIONS));
            poolConfig.setMaxTotal(Integer.parseInt(DB_MAX_CONNECTIONS));
            ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(
                    poolableConnectionFactory, poolConfig);
            poolableConnectionFactory.setPool(connectionPool);

            Class.forName("org.apache.commons.dbcp2.PoolingDriver");
            dbDriver = (PoolingDriver) DriverManager
                    .getDriver("jdbc:apache:commons:dbcp:");
            dbDriver.registerPool(DB_NAME, connectionPool);

            dataSource = new PoolingDataSource<>(connectionPool);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Got error initializing data source: "
                    + ex.getMessage(), ex);
        }
    }

    public static void readJsonConfFile() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject o = (JSONObject) parser.parse(new FileReader("src/main/java/ResourcePools/Postgresconf.json"));


            JSONObject DBConf = (JSONObject) o;

            String user = (String) DBConf.get("user");
            String password = (String) DBConf.get("password");
            String host = (String) DBConf.get("host");
            String port = (String) DBConf.get("port");
            String dbName = (String) DBConf.get("database");

            setDBUser(user);
            setDBPassword(password);
            setDBHost(host);
            setDBPort(port);
            setDBName(dbName);

        if (!formatURL()) {
            throw new Exception("Wrong Format in Postgres.conf");

        }
    }

    public static void readConfFile() throws Exception {
//        System.getProperty("user.dir") +
        String file = "src/main/java/ResourcePools/Postgres.conf";
        java.util.List<String> lines = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\[(.+)\\]");
        Matcher matcher;
        Stream<String> stream = Files.lines(Paths.get(file));
        lines = stream.filter(
                line -> !line.startsWith("#")).collect(Collectors.toList());

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("user")) {
                matcher = pattern.matcher(lines.get(i));
                if (matcher.find())
                    setDBUser(matcher.group(1));
                else
                    throw new Exception("empty user in Postgres.conf");
            }
            if (lines.get(i).startsWith("database")) {
                matcher = pattern.matcher(lines.get(i));
                if (matcher.find())
                    setDBName(matcher.group(1));
                else
                    throw new Exception("empty database name in Postgres.conf");
            }
            if (lines.get(i).startsWith("pass")) {
                matcher = pattern.matcher(lines.get(i));
                matcher.find();
                setDBPassword(matcher.group(1));
            }
            if (lines.get(i).startsWith("host")) {
                matcher = pattern.matcher(lines.get(i));
                if (matcher.find())
                    setDBHost(matcher.group(1));
                else
                    setDBHost("localhost");
            }
            if (lines.get(i).startsWith("port")) {
                matcher = pattern.matcher(lines.get(i));
                if (matcher.find())
                    setDBPort(matcher.group(1));
                else
                    setDBPort("5432");
            }
        }
//        setDBUser("postgres");
//        setDBPassword("jesus");
//        setDBHost("localhost");
//        setDBPort("5432");
//        setDBName("postgres");
        if (!formatURL()) {
            throw new Exception("Wrong Format in Postgres.conf");

        }
    }

    private static boolean formatURL() {
        setDBURL("jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME);
        Pattern pattern = Pattern.compile("^\\w+:\\w+:\\/{2}\\w+:\\d+\\/\\w+(?:\\W|\\w)*$");
        Matcher matcher = pattern.matcher(DB_URL);
        return matcher.matches();
    }

    public static PoolingDataSource<PoolableConnection> getDataSource() {
        return dataSource;
    }

    public static void setDBUser(String name) {
        DB_USERNAME = name;
    }

    public static void setDBPassword(String pass) {
        DB_PASSWORD = pass;
    }

    public static void setDBPort(String port) {
        DB_PORT = port;
    }

    public static void setDBHost(String host) {
        DB_HOST = host;
    }

    public static void setDBURL(String url) {
        DB_URL = url;
    }

    public static void setDBName(String name) {
        DB_NAME = name;
    }
}
