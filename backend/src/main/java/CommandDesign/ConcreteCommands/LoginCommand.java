package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import ResourcePools.PostgresConnection;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.rmi.server.UID;
import java.sql.*;
import java.util.logging.Logger;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class LoginCommand extends Command {
    private final Logger log = Logger.getLogger(LoginCommand.class.getName());
    int user_id;
    private String last_name, first_name, email, phone, encrypted_password;
    private Timestamp created_at;
    private boolean is_active;
    private Date birth_date;

    @Override
    protected void execute() {
        //System.out.println("LOGIN EXEC");

        try{
            String sessionID = URLEncoder.encode(new UID().toString(), "UTF-8");
            String cleaned_session = sessionID.replace("%", "\\%");
            System.out.println("db"+ PostgresConnection.getDataSource());
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(false);

            proc = dbConn.prepareCall("{? = call get_password(?)}");
            proc.setPoolable(true);


            proc.registerOutParameter(1, Types.VARCHAR);
            proc.setString(2, parameters.get("email"));

            proc.execute();
            String enc_password_db = proc.getString(1);

            if (enc_password_db == null) {
//                CommandsHelp.handleError(map.get("app"), map.get("method"),
//                        "Invalid username", map.get("correlation_id"), LOGGER);
//                return;
            }

            String encrypted_password = sha256Hex(parameters.get("password"));

            boolean authenticated = enc_password_db.equals(encrypted_password);

            if(authenticated){
                System.out.println("authenticated");
                CommandsHelp.submit("user", "{\"status\":\"true\", \"code\": \"200\"}", parameters.get("correlation_id"), log);
            }
            else{
                System.out.println("NOT authenticated");
                CommandsHelp.submit("user", "{\"status\":\"false\", \"code\": \"200\"}", parameters.get("correlation_id"), log);
            }

            proc.close();
            dbConn.commit();



        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
