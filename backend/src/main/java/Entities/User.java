package Entities;

import java.sql.Date;
import java.sql.Timestamp;

public class User {
    private int user_id;
    private String last_name, first_name, email, phone;
    private Timestamp created_at;
    private boolean is_active;
    private Date birth_date;
    private String sessionID;
    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public void setBirth_date(Date birth_date) {
        this.birth_date = birth_date;
    }

}
