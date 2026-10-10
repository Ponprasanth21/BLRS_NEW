package com.bornfire.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Admin > Parameters > Email and SMS. ONE row only (param_id = EMAILSMS).
 * email_flg / sms_flg : Y / N
 */
@Entity
@Table(name = "BLRS_EMAIL_SMS_PARAM_TAB")
public class BLRS_EmailSmsParam_Entity {

	@Id
	@Column(name = "PARAM_ID")
	private String param_id;

	// ---- Email ----
	@Column(name = "EMAIL_HOST")
	private String email_host;
	@Column(name = "EMAIL_PORT")
	private String email_port;
	@Column(name = "SENDER_EMAIL")
	private String sender_email;
	@Column(name = "EMAIL_PWD")
	private String email_pwd;
	@Column(name = "EMAIL_FLG")
	private String email_flg;

	// ---- Signature ----
	@Column(name = "AUTH_ID")
	private String auth_id;
	@Column(name = "FOOTER_TEXT")
	private String footer_text;
	@Column(name = "SIGNATURE_NAME")
	private String signature_name;
	@Lob
	@Column(name = "SIGNATURE_IMG")
	private byte[] signature_img;

	// ---- SMS ----
	@Column(name = "SMS_HOST")
	private String sms_host;
	@Column(name = "SMS_USER")
	private String sms_user;
	@Column(name = "SMS_PWD")
	private String sms_pwd;
	@Column(name = "SMS_FLG")
	private String sms_flg;

	// ---- audit ----
	@Column(name = "MODIFY_USER")
	private String modify_user;
	@Column(name = "MODIFY_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modify_time;

	public BLRS_EmailSmsParam_Entity() {
		super();
	}

	public String getParam_id() {
		return param_id;
	}

	public void setParam_id(String param_id) {
		this.param_id = param_id;
	}

	public String getEmail_host() {
		return email_host;
	}

	public void setEmail_host(String email_host) {
		this.email_host = email_host;
	}

	public String getEmail_port() {
		return email_port;
	}

	public void setEmail_port(String email_port) {
		this.email_port = email_port;
	}

	public String getSender_email() {
		return sender_email;
	}

	public void setSender_email(String sender_email) {
		this.sender_email = sender_email;
	}

	public String getEmail_pwd() {
		return email_pwd;
	}

	public void setEmail_pwd(String email_pwd) {
		this.email_pwd = email_pwd;
	}

	public String getEmail_flg() {
		return email_flg;
	}

	public void setEmail_flg(String email_flg) {
		this.email_flg = email_flg;
	}

	public String getAuth_id() {
		return auth_id;
	}

	public void setAuth_id(String auth_id) {
		this.auth_id = auth_id;
	}

	public String getFooter_text() {
		return footer_text;
	}

	public void setFooter_text(String footer_text) {
		this.footer_text = footer_text;
	}

	public String getSignature_name() {
		return signature_name;
	}

	public void setSignature_name(String signature_name) {
		this.signature_name = signature_name;
	}

	public byte[] getSignature_img() {
		return signature_img;
	}

	public void setSignature_img(byte[] signature_img) {
		this.signature_img = signature_img;
	}

	public String getSms_host() {
		return sms_host;
	}

	public void setSms_host(String sms_host) {
		this.sms_host = sms_host;
	}

	public String getSms_user() {
		return sms_user;
	}

	public void setSms_user(String sms_user) {
		this.sms_user = sms_user;
	}

	public String getSms_pwd() {
		return sms_pwd;
	}

	public void setSms_pwd(String sms_pwd) {
		this.sms_pwd = sms_pwd;
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