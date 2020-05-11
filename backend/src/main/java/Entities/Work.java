package Entities;

import java.sql.Date;

public class Work {
    private int id;
    private String institution, job_title;
    private Date start_date, end_date;
    public void setId(int id) {
        this.id = id;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public void setJob_title(String job_title) {
        this.job_title = job_title;
    }

    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }



}
