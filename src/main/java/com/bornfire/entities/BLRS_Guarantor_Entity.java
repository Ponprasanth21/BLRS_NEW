package com.bornfire.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * Guarantor Entity representing loan guarantors.
 */
@Entity
@Table(name = "blrs_guarantor_tab", schema = "blrs")
@IdClass(BLRS_Guarantor_Entity.PK.class)
public class BLRS_Guarantor_Entity {

    @Id
    @Column(name = "ACCOUNT_NUMBER", length = 50)
    private String accountNumber;

    @Id
    @Column(name = "GUARANTOR_ID", length = 50)
    private String guarantorId;

    @Column(name = "GUARANTOR_NAME", length = 200)
    private String guarantorName;

    @Column(name = "MAILING_ADDRESS", length = 500)
    private String mailingAddress;

    @Column(name = "EMAIL_ID", length = 200)
    private String emailId;

    @Column(name = "MOBILE_NO", length = 20)
    private String mobileNo;

    @Column(name = "STATUS", length = 20)
    private String status = "ACTIVE";

    @Column(name = "GUARANTOR_REMINDER", length = 1)
    private String guarantorReminder = "Y";

    public static class PK implements Serializable {
        private static final long serialVersionUID = 1L;
        private String accountNumber;
        private String guarantorId;

        public PK() {
        }

        public PK(String accountNumber, String guarantorId) {
            this.accountNumber = accountNumber;
            this.guarantorId = guarantorId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK)) return false;
            PK pk = (PK) o;
            return Objects.equals(accountNumber, pk.accountNumber) &&
                    Objects.equals(guarantorId, pk.guarantorId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountNumber, guarantorId);
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public String getGuarantorId() {
            return guarantorId;
        }

        public void setGuarantorId(String guarantorId) {
            this.guarantorId = guarantorId;
        }
    }

    public BLRS_Guarantor_Entity() {
    }

    public BLRS_Guarantor_Entity(String accountNumber, String guarantorId, String guarantorName,
                                 String mailingAddress, String emailId, String mobileNo,
                                 String status, String guarantorReminder) {
        this.accountNumber = accountNumber;
        this.guarantorId = guarantorId;
        this.guarantorName = guarantorName;
        this.mailingAddress = mailingAddress;
        this.emailId = emailId;
        this.mobileNo = mobileNo;
        this.status = status != null ? status : "ACTIVE";
        this.guarantorReminder = guarantorReminder != null ? guarantorReminder : "Y";
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getGuarantorId() {
        return guarantorId;
    }

    public void setGuarantorId(String guarantorId) {
        this.guarantorId = guarantorId;
    }

    public String getGuarantorName() {
        return guarantorName;
    }

    public void setGuarantorName(String guarantorName) {
        this.guarantorName = guarantorName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGuarantorReminder() {
        return guarantorReminder;
    }

    public void setGuarantorReminder(String guarantorReminder) {
        this.guarantorReminder = guarantorReminder;
    }
}
