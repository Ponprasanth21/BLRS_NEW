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

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Recovery entity tracking repayments and recovery events.
 * (accountNumber, recordDate, loanOutstanding, principal, interest, total,
 *  reminderDate, recoveryDate, amount, overdueAfter, age, status)
 */
@Entity
@Table(name = "BLRS_RECOVERY_TAB")
public class BLRS_Recovery_Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RECOVERY_ID", length = 100)
    private String recoveryId;

    @Column(name = "ACCOUNT_NUMBER", length = 50, nullable = false)
    private String accountNumber;

    @Column(name = "RECORD_DATE")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date recordDate;

    @Column(name = "LOAN_OUTSTANDING", precision = 20, scale = 2)
    private BigDecimal loanOutstanding = BigDecimal.ZERO;

    @Column(name = "PRINCIPAL", precision = 20, scale = 2)
    private BigDecimal principal = BigDecimal.ZERO;

    @Column(name = "INTEREST", precision = 20, scale = 2)
    private BigDecimal interest = BigDecimal.ZERO;

    @Column(name = "TOTAL", precision = 20, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "REMINDER_DATE")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date reminderDate;

    @Column(name = "RECOVERY_DATE")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date recoveryDate;

    @Column(name = "AMOUNT", precision = 20, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "OVERDUE_AFTER", precision = 20, scale = 2)
    private BigDecimal overdueAfter = BigDecimal.ZERO;

    @Column(name = "AGE")
    private Integer age = 0;

    @Column(name = "STATUS", length = 30)
    private String status; // RECOVERED, PARTIAL, OVERDUE

    @Column(name = "CREATED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime = new Date();

    public BLRS_Recovery_Entity() {
    }

    public String getRecoveryId() {
        return recoveryId;
    }

    public void setRecoveryId(String recoveryId) {
        this.recoveryId = recoveryId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    public BigDecimal getLoanOutstanding() {
        return loanOutstanding;
    }

    public void setLoanOutstanding(BigDecimal loanOutstanding) {
        this.loanOutstanding = loanOutstanding;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Date getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(Date reminderDate) {
        this.reminderDate = reminderDate;
    }

    public Date getRecoveryDate() {
        return recoveryDate;
    }

    public void setRecoveryDate(Date recoveryDate) {
        this.recoveryDate = recoveryDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getOverdueAfter() {
        return overdueAfter;
    }

    public void setOverdueAfter(BigDecimal overdueAfter) {
        this.overdueAfter = overdueAfter;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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
