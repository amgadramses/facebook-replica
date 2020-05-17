package ResourcePools;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class PostgreSQLInitializer {
    public static void initPostgreSQL() {
        String sqlPath = "src/main/java/ResourcePools/queries.sql";

        Connection dbConn = null;
        Statement statement = null;
        try {
            dbConn = PostgresConnection.getDataSource().getConnection();
            statement = dbConn.createStatement();
            statement.executeUpdate("DROP DATABASE mydb");
            ScriptRunner sr = new ScriptRunner(dbConn);
            Reader reader = null;
            try {
                reader = new BufferedReader(
                        new FileReader(sqlPath));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }

            // Exctute script
            sr.runScript(reader);

        } catch (Exception e) {
            System.err.println("Failed to Execute" + sqlPath
                    + " The error is " + e.getMessage());
        } finally {
            PostgresConnection.disconnect(null, (PreparedStatement) statement, dbConn, null);

        }
    }
}
