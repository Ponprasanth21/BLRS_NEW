package com.bornfire.entities;

import javax.persistence.*;
import java.util.Date;

/**
 * Audit trail of every Email / SMS attempt. One row per (channel, account,
 * reminder-type, attempt).
 */
@Entity
@Table(name = "BLRS_NOTIFICATION_LOG_TAB", schema = "blrs")
public class BLRS_NotificationLog_Entity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "LOG_ID")
	private Long logId;

	@Column(name = "CHANNEL", length = 10)
	private String channel; // EMAIL / SMS
	@Column(name = "REMINDER_NO", length = 10)
	private String reminderNo; // REM1/REM2/REM3
	@Column(name = "ACCOUNT_NO", length = 30)
	private String accountNo;
	@Column(name = "CUSTOMER_ID", length = 30)
	private String customerId;
	@Column(name = "RECIPIENT", length = 200)
	private String recipient;
	@Column(name = "SUBJECT", length = 500)
	private String subject;
	@Column(name = "STATUS", length = 15)
	private String status; // SENT / FAILED
	@Column(name = "ERROR_MSG", length = 500)
	private String errorMsg;
	@Column(name = "ATTACHMENT_NAME", length = 200)
	private String attachmentName;
	@Column(name = "SENT_TIME")
	private Date sentTime;
	@Column(name = "ENTRY_USER", length = 30)
	private String entryUser;

	// --- getters / setters ---
	public Long getLogId() {
		return logId;
	}

	public void setLogId(Long logId) {
		this.logId = logId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getReminderNo() {
		return reminderNo;
	}

	public void setReminderNo(String reminderNo) {
		this.reminderNo = reminderNo;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public Date getSentTime() {
		return sentTime;
	}

	public void setSentTime(Date sentTime) {
		this.sentTime = sentTime;
	}

	public String getEntryUser() {
		return entryUser;
	}

	public void setEntryUser(String entryUser) {
		this.entryUser = entryUser;
	}
}