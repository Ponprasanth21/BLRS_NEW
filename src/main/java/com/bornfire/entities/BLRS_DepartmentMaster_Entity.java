package com.bornfire.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Department Master entity loaded from Finacle.
 * Departments: SAVN, EDUC, WMA, HTLH, CHCL, AMTE, MRA, PSIC, MPL, PLCE, etc.
 */
@Entity
@Table(name = "BLRS_DEPARTMENT_MASTER_TAB")
public class BLRS_DepartmentMaster_Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DEPARTMENT_CODE", length = 50)
    private String departmentCode;

    @Column(name = "DEPARTMENT_NAME", length = 200)
    private String departmentName;

    @Column(name = "STATUS", length = 20)
    private String status = "ACTIVE";

    @Column(name = "CREATED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime = new Date();

    public BLRS_DepartmentMaster_Entity() {
    }

    public BLRS_DepartmentMaster_Entity(String departmentCode, String departmentName, String status) {
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.status = status;
        this.createdTime = new Date();
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
