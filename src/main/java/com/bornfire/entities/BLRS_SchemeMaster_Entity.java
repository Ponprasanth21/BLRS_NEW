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
 * Scheme Master entity loaded from Finacle.
 * Schemes: CLN2, CLN4, CPL1, ELN1, MQL1, PLN1, PPL1, SSL1, PLBPA, FINLI, DPEMI, HOULN, LNBPA, LNEST, SMEAG, SMELG, TLEMI.
 */
@Entity
@Table(name = "BLRS_SCHEME_MASTER_TAB")
public class BLRS_SchemeMaster_Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "SCHEME_CODE", length = 50)
    private String schemeCode;

    @Column(name = "SCHEME_DESC", length = 200)
    private String schemeDesc;

    @Column(name = "STATUS", length = 20)
    private String status = "ACTIVE";

    @Column(name = "CREATED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime = new Date();

    public BLRS_SchemeMaster_Entity() {
    }

    public BLRS_SchemeMaster_Entity(String schemeCode, String schemeDesc, String status) {
        this.schemeCode = schemeCode;
        this.schemeDesc = schemeDesc;
        this.status = status;
        this.createdTime = new Date();
    }

    public String getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(String schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getSchemeDesc() {
        return schemeDesc;
    }

    public void setSchemeDesc(String schemeDesc) {
        this.schemeDesc = schemeDesc;
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
