package com.bornfire.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Reminder entity tracking generated reminders with duplicate prevention.
 * (reminderId, accountNumber, reminderNo, reminderDate, overdueAmount, ageDays, loaneeName, guarantorName, generatedFile, emailSent, smsSent)
 *
 * Ground Rule 4: Unique constraint on (accountNumber, reminderNo).
 */
@Entity
@Table(
    name = "BLRS_REMINDER_TAB",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_REMINDER_ACC_NO", columnNames = {"ACCOUNT_NUMBER", "REMINDER_NO"})
    }
)
public class BLRS_Reminder_Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "REMINDER_ID", length = 100)
    private String reminderId;

    @Column(name = "ACCOUNT_NUMBER", length = 50, nullable = false)
    private String accountNumber;

    @Column(name = "REMINDER_NO", length = 20, nullable = false)
    private String reminderNo; // REM1, REM2, REM3

    @Column(name = "REMINDER_DATE")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date reminderDate;

    @Column(name = "OVERDUE_AMOUNT", precision = 20, scale = 2)
    private BigDecimal overdueAmount;

    @Column(name = "AGE_DAYS")
    private Integer ageDays;

    @Column(name = "LOANEE_NAME", length = 200)
    private String loaneeName;

    @Column(name = "GUARANTOR_NAME", length = 200)
    private String guarantorName;

    @Column(name = "GENERATED_FILE", length = 300)
    private String generatedFile;

    @Column(name = "EMAIL_SENT", length = 1)
    private String emailSent = "N";

    @Column(name = "SMS_SENT", length = 1)
    private String smsSent = "N";

    @Column(name = "CUSTOMER_ID", length = 50)
    private String customerId;

    @Column(name = "SCHEME_CODE", length = 50)
    private String schemeCode;

    @Column(name = "DEPARTMENT", length = 50)
    private String department;

    @Column(name = "CREATED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime = new Date();

    public BLRS_Reminder_Entity() {
    }

    public BLRS_Reminder_Entity(String reminderId, String accountNumber, String reminderNo, Date reminderDate,
                                BigDecimal overdueAmount, Integer ageDays, String loaneeName,
                                String guarantorName, String generatedFile, String emailSent, String smsSent) {
        this.reminderId = reminderId;
        this.accountNumber = accountNumber;
        this.reminderNo = reminderNo;
        this.reminderDate = reminderDate;
        this.overdueAmount = overdueAmount;
        this.ageDays = ageDays;
        this.loaneeName = loaneeName;
        this.guarantorName = guarantorName;
        this.generatedFile = generatedFile;
        this.emailSent = emailSent;
        this.smsSent = smsSent;
        this.createdTime = new Date();
    }

    public String getReminderId() {
        return reminderId;
    }

    public void setReminderId(String reminderId) {
        this.reminderId = reminderId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getReminderNo() {
        return reminderNo;
    }

    public void setReminderNo(String reminderNo) {
        this.reminderNo = reminderNo;
    }

    public Date getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(Date reminderDate) {
        this.reminderDate = reminderDate;
    }

    public BigDecimal getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(BigDecimal overdueAmount) {
        this.overdueAmount = overdueAmount;
    }

    public Integer getAgeDays() {
        return ageDays;
    }

    public void setAgeDays(Integer ageDays) {
        this.ageDays = ageDays;
    }

    public String getLoaneeName() {
        return loaneeName;
    }

    public void setLoaneeName(String loaneeName) {
        this.loaneeName = loaneeName;
    }

    public String getGuarantorName() {
        return guarantorName;
    }

    public void setGuarantorName(String guarantorName) {
        this.guarantorName = guarantorName;
    }

    public String getGeneratedFile() {
        return generatedFile;
    }

    public void setGeneratedFile(String generatedFile) {
        this.generatedFile = generatedFile;
    }

    public String getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(String emailSent) {
        this.emailSent = emailSent;
    }

    public String getSmsSent() {
        return smsSent;
    }

    public void setSmsSent(String smsSent) {
        this.smsSent = smsSent;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(String schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
