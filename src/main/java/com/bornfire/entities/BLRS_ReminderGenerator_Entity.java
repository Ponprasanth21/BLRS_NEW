package com.bornfire.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BLRS_REMINDER_GENERATOR_TAB")
public class BLRS_ReminderGenerator_Entity {

	@Id
	@Column(name = "ACCT_NUM")
	private String acct_num;

	@Column(name = "SCHEM_CODE")
	private String schem_code;

	@Column(name = "CUST_ID")
	private String cust_id;

	@Column(name = "ACCT_NAME")
	private String acct_name;

	@Column(name = "OVERDUE")
	private BigDecimal overdue;

	@Column(name = "DEPT")
	private String dept;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "REM_SEND")
	private String rem_send;

	@Column(name = "ACC_NUMBER")
	private String acc_number;

	@Column(name = "REM_TYPE")
	private String rem_type;

//	@Column(name = "EMAIL_FLG")
//	private String email_flg;
//
//	@Column(name = "SMS_FLG")
//	private String sms_flg;

	public BLRS_ReminderGenerator_Entity() {
		super();
	}

	public String getAcct_num() {
		return acct_num;
	}

	public void setAcct_num(String acct_num) {
		this.acct_num = acct_num;
	}

	public String getSchem_code() {
		return schem_code;
	}

	public void setSchem_code(String schem_code) {
		this.schem_code = schem_code;
	}

	public String getCust_id() {
		return cust_id;
	}

	public void setCust_id(String cust_id) {
		this.cust_id = cust_id;
	}

	public String getAcct_name() {
		return acct_name;
	}

	public void setAcct_name(String acct_name) {
		this.acct_name = acct_name;
	}

	public BigDecimal getOverdue() {
		return overdue;
	}

	public void setOverdue(BigDecimal overdue) {
		this.overdue = overdue;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRem_send() {
		return rem_send;
	}

	public void setRem_send(String rem_send) {
		this.rem_send = rem_send;
	}

	public String getAcc_number() {
		return acc_number;
	}

	public void setAcc_number(String acc_number) {
		this.acc_number = acc_number;
	}

	public String getRem_type() {
		return rem_type;
	}

	public void setRem_type(String rem_type) {
		this.rem_type = rem_type;
	}

//	public String getEmail_flg() {
//		return email_flg;
//	}
//
//	public void setEmail_flg(String email_flg) {
//		this.email_flg = email_flg;
//	}
//
//	public String getSms_flg() {
//		return sms_flg;
//	}
//
//	public void setSms_flg(String sms_flg) {
//		this.sms_flg = sms_flg;
//	}
}
