package com.bornfire.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Overdue Account snapshot entity extracted from Finacle / computed in LRS.
 * (accountNumber, recordDate, schemeCode, department, loanAmount, outstandingBalance,
 *  interestRate, totalArrears, capitalArrears, interestArrears, monthArrears,
 *  overdueAmount, ageDays, dueDate, status OVERDUE/PAID)
 */
@Entity
@Table(name = "BLRS_OVERDUE_ACCOUNT_TAB")
@IdClass(BLRS_OverdueAccount_Entity.PK.class)
public class BLRS_OverdueAccount_Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ACCOUNT_NUMBER", length = 50)
    private String accountNumber;

    @Id
    @Column(name = "RECORD_DATE")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date recordDate;

    @Column(name = "CUSTOMER_ID", length = 50)
    private String customerId;

    @Column(name = "BORROWER_NAME", length = 200)
    private String borrowerName;

    @Column(name = "SCHEME_CODE", length = 50)
    private String schemeCode;

    @Column(name = "DEPARTMENT", length = 50)
    private String department;

    @Column(name = "LOAN_AMOUNT", precision = 20, scale = 2)
    private BigDecimal loanAmount = BigDecimal.ZERO;

    @Column(name = "OUTSTANDING_BALANCE", precision = 20, scale = 2)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(name = "INTEREST_RATE", precision = 10, scale = 4)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(name = "TOTAL_ARREARS", precision = 20, scale = 2)
    private BigDecimal totalArrears = BigDecimal.ZERO;

    @Column(name = "CAPITAL_ARREARS", precision = 20, scale = 2)
    private BigDecimal capitalArrears = BigDecimal.ZERO;

    @Column(name = "INTEREST_ARREARS", precision = 20, scale = 2)
    private BigDecimal interestArrears = BigDecimal.ZERO;

    @Column(name = "MONTH_ARREARS")
    private Integer monthArrears = 0;

    @Column(name = "OVERDUE_AMOUNT", precision = 20, scale = 2)
    private BigDecimal overdueAmount = BigDecimal.ZERO;

    @Column(name = "AGE_DAYS")
    private Integer ageDays = 0;

    @Column(name = "DUE_DATE")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date dueDate;

    @Column(name = "STATUS", length = 20)
    private String status; // OVERDUE, PAID

    public static class PK implements Serializable {
        private static final long serialVersionUID = 1L;
        private String accountNumber;
        private Date recordDate;

        public PK() {
        }

        public PK(String accountNumber, Date recordDate) {
            this.accountNumber = accountNumber;
            this.recordDate = recordDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK)) return false;
            PK pk = (PK) o;
            return Objects.equals(accountNumber, pk.accountNumber) &&
                    Objects.equals(recordDate, pk.recordDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountNumber, recordDate);
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
    }

    public BLRS_OverdueAccount_Entity() {
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
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

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getTotalArrears() {
        return totalArrears;
    }

    public void setTotalArrears(BigDecimal totalArrears) {
        this.totalArrears = totalArrears;
    }

    public BigDecimal getCapitalArrears() {
        return capitalArrears;
    }

    public void setCapitalArrears(BigDecimal capitalArrears) {
        this.capitalArrears = capitalArrears;
    }

    public BigDecimal getInterestArrears() {
        return interestArrears;
    }

    public void setInterestArrears(BigDecimal interestArrears) {
        this.interestArrears = interestArrears;
    }

    public Integer getMonthArrears() {
        return monthArrears;
    }

    public void setMonthArrears(Integer monthArrears) {
        this.monthArrears = monthArrears;
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

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
