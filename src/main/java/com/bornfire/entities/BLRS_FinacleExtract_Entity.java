package com.bornfire.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Staging Entity for Finacle User Extraction (FIN001).
 * Used by Admin > User Profile to search and import employee details directly from Finacle.
 */
@Entity
@Table(name = "BLRS_FINACLE_EXTRACT_TAB")
public class BLRS_FinacleExtract_Entity {

    @Id
    @Column(name = "USER_ID", length = 50)
    private String user_id;

    @Column(name = "USER_NAME", length = 150)
    private String user_name;

    @Column(name = "EMP_ID", length = 50)
    private String emp_id;

    @Column(name = "EMAIL_ID", length = 150)
    private String email_id;

    @Column(name = "MOBILE_NO", length = 30)
    private String mobile_no;

    @Column(name = "BRANCH_CODE", length = 20)
    private String branch_code;

    @Column(name = "BRANCH_NAME", length = 100)
    private String branch_name;

    @Column(name = "DEPARTMENT", length = 50)
    private String department;

    @Column(name = "DESIGNATION", length = 100)
    private String designation;

    @Column(name = "STATUS", length = 20)
    private String status;

    public BLRS_FinacleExtract_Entity() {
    }

    public BLRS_FinacleExtract_Entity(String user_id, String user_name, String emp_id, String email_id,
                                      String mobile_no, String branch_code, String branch_name,
                                      String department, String designation, String status) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.emp_id = emp_id;
        this.email_id = email_id;
        this.mobile_no = mobile_no;
        this.branch_code = branch_code;
        this.branch_name = branch_name;
        this.department = department;
        this.designation = designation;
        this.status = status;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getEmp_id() {
        return emp_id;
    }

    public void setEmp_id(String emp_id) {
        this.emp_id = emp_id;
    }

    public String getEmail_id() {
        return email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getBranch_code() {
        return branch_code;
    }

    public void setBranch_code(String branch_code) {
        this.branch_code = branch_code;
    }

    public String getBranch_name() {
        return branch_name;
    }

    public void setBranch_name(String branch_name) {
        this.branch_name = branch_name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
