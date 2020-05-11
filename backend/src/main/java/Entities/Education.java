package Entities;

import java.sql.Date;

public class Education {

    private int id;
    private String institution, degree;
    private Date start_date, end_date;
    public void setId(int id) {
        this.id = id;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }



}
