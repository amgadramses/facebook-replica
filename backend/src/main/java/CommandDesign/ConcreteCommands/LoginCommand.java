package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Entities.User;
import Redis.UserCache;
import ResourcePools.PostgresConnection;
import com.fasterxml.jackson.core.JsonProcessingException;

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
    private String last_name, first_name, email, phone;
    private Timestamp created_at;
    private boolean is_active;
    private Date birth_date;

    @Override
    protected void execute() {
        //System.out.println("LOGIN EXEC");

        try {
            System.out.println("db" + PostgresConnection.getDataSource());
            dbConn = PostgresConnection.getDataSource().getConnection();
            dbConn.setAutoCommit(false);

            proc = dbConn.prepareCall("{? = call get_password(?)}");
            proc.setPoolable(true);


            proc.registerOutParameter(1, Types.VARCHAR);
            proc.setString(2, parameters.get("email").toLowerCase());

            proc.execute();

            String enc_password_db = proc.getString(1);
            proc.close();
            dbConn.commit();
            if (enc_password_db == null) {
//                CommandsHelp.handleError(map.get("app"), map.get("method"),
//                        "Invalid username", map.get("correlation_id"), LOGGER);
//                return;
            }

            String encrypted_password = sha256Hex(parameters.get("password"));

            boolean authenticated = enc_password_db.equals(encrypted_password);

            if (authenticated) {
                proc = dbConn.prepareCall("{? = call login(?)}");
                proc.setPoolable(true);
                proc.registerOutParameter(1, Types.OTHER);
                proc.setString(2, parameters.get("email"));
                proc.execute();
                set = (ResultSet) proc.getObject(1);
                proc.close();
                dbConn.commit();
                User user = new User();

                while (set.next()) {
                    user_id = set.getInt("user_id");
                    email = set.getString("email");
                    first_name = set.getString("first_name");
                    last_name = set.getString("last_name");
                    created_at = set.getTimestamp("created_at");
                    is_active = set.getBoolean("is_active");
                    phone = set.getString("phone");
                    birth_date = set.getDate("birth_date");
                }
                user.setUser_id(user_id);
                user.setEmail(email);
                user.setFirst_name(first_name);
                user.setLast_name(last_name);
                user.setCreated_at(created_at);
                user.setIs_active(is_active);
                user.setPhone(phone);
                user.setBirth_date(birth_date);


                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "ok");
                responseJson.put("code", "200");
                responseJson.set("user", nf.pojoNode(user));

//                toBeCached = ObjectToMap(user);

                UserCache.userCache.set("user", user_id + "");

            } else {
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "Bad Request");
                responseJson.put("code", "400");
                responseJson.put("message", "Invalid Password");
            }
            try {
                CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {

            e.printStackTrace();
        } finally {
            PostgresConnection.disconnect(set, proc, dbConn, null);
        }

    }
}
