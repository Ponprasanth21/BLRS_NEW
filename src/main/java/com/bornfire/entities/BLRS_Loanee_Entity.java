package com.bornfire.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * Loanee Entity supporting up to 3 loanees per loan account (Primary, Co-borrower 1, Co-borrower 2).
 */
@Entity
@Table(name = "blrs_loanee_tab", schema = "blrs")
@IdClass(BLRS_Loanee_Entity.PK.class)
public class BLRS_Loanee_Entity {

    @Id
    @Column(name = "ACCOUNT_NUMBER", length = 50)
    private String accountNumber;

    @Id
    @Column(name = "LOANEE_INDEX")
    private Integer loaneeIndex;

    @Column(name = "CUSTOMER_ID", length = 50)
    private String customerId;

    @Column(name = "BORROWER_NAME", length = 200)
    private String borrowerName;

    @Column(name = "MAILING_ADDRESS", length = 500)
    private String mailingAddress;

    @Column(name = "EMAIL_ID", length = 200)
    private String emailId;

    @Column(name = "MOBILE_NO", length = 20)
    private String mobileNo;

    @Column(name = "LOANEE_REMINDER", length = 1)
    private String loaneeReminder = "Y";

    public static class PK implements Serializable {
        private static final long serialVersionUID = 1L;
        private String accountNumber;
        private Integer loaneeIndex;

        public PK() {
        }

        public PK(String accountNumber, Integer loaneeIndex) {
            this.accountNumber = accountNumber;
            this.loaneeIndex = loaneeIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK)) return false;
            PK pk = (PK) o;
            return Objects.equals(accountNumber, pk.accountNumber) &&
                    Objects.equals(loaneeIndex, pk.loaneeIndex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountNumber, loaneeIndex);
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public Integer getLoaneeIndex() {
            return loaneeIndex;
        }

        public void setLoaneeIndex(Integer loaneeIndex) {
            this.loaneeIndex = loaneeIndex;
        }
    }

    public BLRS_Loanee_Entity() {
    }

    public BLRS_Loanee_Entity(String accountNumber, Integer loaneeIndex, String customerId,
                              String borrowerName, String mailingAddress, String emailId,
                              String mobileNo, String loaneeReminder) {
        this.accountNumber = accountNumber;
        this.loaneeIndex = loaneeIndex;
        this.customerId = customerId;
        this.borrowerName = borrowerName;
        this.mailingAddress = mailingAddress;
        this.emailId = emailId;
        this.mobileNo = mobileNo;
        this.loaneeReminder = loaneeReminder != null ? loaneeReminder : "Y";
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Integer getLoaneeIndex() {
        return loaneeIndex;
    }

    public void setLoaneeIndex(Integer loaneeIndex) {
        this.loaneeIndex = loaneeIndex;
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

    public String getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(String mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getLoaneeReminder() {
        return loaneeReminder;
    }

    public void setLoaneeReminder(String loaneeReminder) {
        this.loaneeReminder = loaneeReminder;
    }
}
