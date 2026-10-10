package com.bornfire.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Admin > Parameters > Reminder. ONE row per reminder level (REM1, REM2, REM3).
 * All flags : Y / N
 */
@Entity
@Table(name = "BLRS_REMINDER_PARAM_TAB")
public class BLRS_ReminderParam_Entity {

	@Id
	@Column(name = "REMINDER_NO")
	private String reminder_no;

	@Column(name = "DOWNLOAD_FLG")
	private String download_flg;
	@Column(name = "SIGNATURE_FLG")
	private String signature_flg;
	@Column(name = "EMAIL_FLG")
	private String email_flg;
	@Column(name = "SMS_FLG")
	private String sms_flg;

	@Column(name = "MODIFY_USER")
	private String modify_user;
	@Column(name = "MODIFY_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modify_time;

	public BLRS_ReminderParam_Entity() {
		super();
	}

	public String getReminder_no() {
		return reminder_no;
	}

	public void setReminder_no(String reminder_no) {
		this.reminder_no = reminder_no;
	}

	public String getDownload_flg() {
		return download_flg;
	}

	public void setDownload_flg(String download_flg) {
		this.download_flg = download_flg;
	}

	public String getSignature_flg() {
		return signature_flg;
	}

	public void setSignature_flg(String signature_flg) {
		this.signature_flg = signature_flg;
	}

	public String getEmail_flg() {
		return email_flg;
	}

	public void setEmail_flg(String email_flg) {
		this.email_flg = email_flg;
	}

	public String getSms_flg() {
		return sms_flg;
	}

	public void setSms_flg(String sms_flg) {
		this.sms_flg = sms_flg;
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
}