package com.bornfire.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Base Audit Entity (@MappedSuperclass) providing standardized maker-checker audit fields.
 * Compatible with existing LRS DB schema columns (ENTRY_USER, AUTH_USER, ENTITY_FLG)
 * while providing standard spec getters and aliases (verifyUser, recordStatus).
 */
@MappedSuperclass
public abstract class BaseAuditEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ENTRY_USER", length = 50)
    protected String entry_user;

    @Column(name = "ENTRY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    protected Date entry_time;

    @Column(name = "MODIFY_USER", length = 50)
    protected String modify_user;

    @Column(name = "MODIFY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    protected Date modify_time;

    @Column(name = "AUTH_USER", length = 50)
    protected String auth_user;

    @Column(name = "AUTH_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    protected Date auth_time;

    @Column(name = "ENTITY_FLG", length = 20)
    protected String entity_flg = "N"; // "N" (UNVERIFIED) / "Y" (VERIFIED)

    public BaseAuditEntity() {
    }

    // --- Standard Legacy Getters/Setters ---

    public String getEntry_user() {
        return entry_user;
    }

    public void setEntry_user(String entry_user) {
        this.entry_user = entry_user;
    }

    public Date getEntry_time() {
        return entry_time;
    }

    public void setEntry_time(Date entry_time) {
        this.entry_time = entry_time;
    }

    public String getModify_user() {
        return modify_user;
    }

    public void setModify_user(String modify_user) {
        this.modify_user = modify_user;
    }

    public Date getModify_time() {
        return modify_time;
    }

    public void setModify_time(Date modify_time) {
        this.modify_time = modify_time;
    }

    public String getAuth_user() {
        return auth_user;
    }

    public void setAuth_user(String auth_user) {
        this.auth_user = auth_user;
    }

    public Date getAuth_time() {
        return auth_time;
    }

    public void setAuth_time(Date auth_time) {
        this.auth_time = auth_time;
    }

    public String getEntity_flg() {
        return entity_flg;
    }

    public void setEntity_flg(String entity_flg) {
        this.entity_flg = entity_flg;
    }

    // --- Specification Aliases ---

    public String getEntryUser() {
        return entry_user;
    }

    public void setEntryUser(String entryUser) {
        this.entry_user = entryUser;
    }

    public Date getEntryTime() {
        return entry_time;
    }

    public void setEntryTime(Date entryTime) {
        this.entry_time = entryTime;
    }

    public String getModifyUser() {
        return modify_user;
    }

    public void setModifyUser(String modifyUser) {
        this.modify_user = modifyUser;
    }

    public Date getModifyTime() {
        return modify_time;
    }

    public void setModifyTime(Date modifyTime) {
        this.modify_time = modifyTime;
    }

    public String getVerifyUser() {
        return auth_user;
    }

    public void setVerifyUser(String verifyUser) {
        this.auth_user = verifyUser;
    }

    public Date getVerifyTime() {
        return auth_time;
    }

    public void setVerifyTime(Date verifyTime) {
        this.auth_time = verifyTime;
    }

    public String getRecordStatus() {
        if ("Y".equalsIgnoreCase(entity_flg) || "VERIFIED".equalsIgnoreCase(entity_flg)) {
            return "VERIFIED";
        }
        return "UNVERIFIED";
    }

    public void setRecordStatus(String status) {
        if ("VERIFIED".equalsIgnoreCase(status) || "Y".equalsIgnoreCase(status)) {
            this.entity_flg = "Y";
        } else {
            this.entity_flg = "N";
        }
    }

    public boolean isVerified() {
        return "Y".equalsIgnoreCase(this.entity_flg) || "VERIFIED".equalsIgnoreCase(this.entity_flg);
    }
}
