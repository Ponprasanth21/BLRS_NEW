package com.bornfire.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "blrs_guarantor_tab", schema = "blrs")
public class BLRS_Guarantor_Entity {

	@Id
	@Column(name = "GUARANTOR_ID", length = 80)
	private String guarantorId;

	@Column(name = "ACCOUNT_NUMBER", length = 50)
	private String accountNumber;

	@Column(name = "CUSTOMER_ID", length = 50)
	private String customerId;

	@Column(name = "GUARANTOR_SL_NO")
	private Integer guarantorSlNo;

	@Column(name = "GUARANTOR_CUSTOMER_ID", length = 50)
	private String guarantorCustomerId;

	@Column(name = "GUARANTOR_NAME", length = 200)
	private String guarantorName;

	@Column(name = "GUARANTOR_NIC", length = 50)
	private String guarantorNic;

	@Column(name = "MAILING_ADDRESS", length = 500)
	private String mailingAddress;

	@Column(name = "MOBILE_NO", length = 20)
	private String mobileNo;

	@Column(name = "EMAIL_ID", length = 200)
	private String emailId;

	@Column(name = "RELATIONSHIP", length = 100)
	private String relationship;

	@Column(name = "GUARANTEE_AMOUNT", precision = 20, scale = 2)
	private BigDecimal guaranteeAmount;

	@Column(name = "GUARANTOR_STATUS", length = 50)
	private String guarantorStatus;

	@Column(name = "GUARANTOR_REMINDER", length = 10)
	private String guarantorReminder;

	public BLRS_Guarantor_Entity() {
	}

	public String getGuarantorId() {
		return guarantorId;
	}

	public void setGuarantorId(String guarantorId) {
		this.guarantorId = guarantorId;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Integer getGuarantorSlNo() {
		return guarantorSlNo;
	}

	public void setGuarantorSlNo(Integer guarantorSlNo) {
		this.guarantorSlNo = guarantorSlNo;
	}

	public String getGuarantorCustomerId() {
		return guarantorCustomerId;
	}

	public void setGuarantorCustomerId(String guarantorCustomerId) {
		this.guarantorCustomerId = guarantorCustomerId;
	}

	public String getGuarantorName() {
		return guarantorName;
	}

	public void setGuarantorName(String guarantorName) {
		this.guarantorName = guarantorName;
	}

	public String getGuarantorNic() {
		return guarantorNic;
	}

	public void setGuarantorNic(String guarantorNic) {
		this.guarantorNic = guarantorNic;
	}

	public String getMailingAddress() {
		return mailingAddress;
	}

	public void setMailingAddress(String mailingAddress) {
		this.mailingAddress = mailingAddress;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public BigDecimal getGuaranteeAmount() {
		return guaranteeAmount;
	}

	public void setGuaranteeAmount(BigDecimal guaranteeAmount) {
		this.guaranteeAmount = guaranteeAmount;
	}

	public String getGuarantorStatus() {
		return guarantorStatus;
	}

	public void setGuarantorStatus(String guarantorStatus) {
		this.guarantorStatus = guarantorStatus;
	}

	public String getGuarantorReminder() {
		return guarantorReminder;
	}

	public void setGuarantorReminder(String guarantorReminder) {
		this.guarantorReminder = guarantorReminder;
	}
}