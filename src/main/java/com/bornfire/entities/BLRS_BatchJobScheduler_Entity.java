package com.bornfire.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * ONE entity for the ONE table BLRS_BATCH_JOB_SCHEDULER_TAB (list, add, modify,
 * verify, delete, execution).
 *
 * BLRSBatchScheduler.jsp field -> entity field / DB column PROCESS_ID -> job_id
 * DESC -> description JOB_TYPE -> job_type SCHM_CODE -> schm_code PERIODICITY
 * -> periodicity START_DATE -> start_date END_DATE -> end_date JOB_NAME ->
 * job_name LAST_RUN_DATE / NEXT_RUN_DATE -> last_run_date / next_run_date
 * DEPARTMENT -> department REM_TYPE -> rem_type STATUS -> status (ACTIVE /
 * SUSPEND / DELETE) EMAIL_ID -> email_id PASSWORD -> email_pwd HEADER ->
 * email_header FOOTER -> email_footer EMAIL_FLG -> email_flg SMS_FLG -> sms_flg
 * USER_CRE -> entry_user (column USER_CRE) PERIOD, DAY, HOUR, HOUR1
 * -> @Transient (period, day, hour, hour1) : NOT columns, only used to build
 * NEXT_RUN_DATE ENTITY_FLG (Y = verified), DEL_FLG (Y = deleted), ENTRY_TIME,
 * MODIFY_USER/TIME, AUTH_USER/TIME : maker-checker audit
 */
@Entity
@Table(name = "BLRS_BATCH_JOB_SCHEDULER_TAB")
public class BLRS_BatchJobScheduler_Entity {

	@Id
	@Column(name = "JOB_ID")
	private String job_id;

	@Column(name = "JOB_NAME")
	private String job_name;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "JOB_TYPE")
	private String job_type;

	@Column(name = "SCHM_CODE")
	private String schm_code;

	@Column(name = "REM_TYPE")
	private String reminder_type;

	@Column(name = "DEPARTMENT")
	private String department;

	@Column(name = "PERIODICITY")
	private String periodicity;

	@Column(name = "START_DATE")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date start_date;

	@Column(name = "END_DATE")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date end_date;

	@Column(name = "LAST_RUN_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
	private Date last_run_date;

	@Column(name = "NEXT_RUN_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
	private Date next_run_date;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "EMAIL_FLG")
	private String email_flg;

	@Column(name = "SMS_FLG")
	private String sms_flg;

	@Column(name = "EMAIL_ID")
	private String email_id;

	@Column(name = "EMAIL_PWD")
	private String email_pwd;

	@Column(name = "EMAIL_HEADER")
	private String email_header;

	@Column(name = "EMAIL_FOOTER")
	private String email_footer;

	@Column(name = "ENTITY_FLG")
	private String entity_flg;

	@Column(name = "DEL_FLG")
	private String del_flg;

	@Column(name = "USER_CRE")
	private String entry_user;

	@Column(name = "ENTRY_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date entry_time;

	@Column(name = "MODIFY_USER")
	private String modify_user;

	@Column(name = "MODIFY_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modify_time;

	@Column(name = "AUTH_USER")
	private String auth_user;

	@Column(name = "AUTH_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date auth_time;

	// ---- Periodicity pop-up (BLRSBatchScheduler.jsp : PERIOD, DAY, HOUR, HOUR1)
	// ----
	// Not DB columns. They only decide NEXT_RUN_DATE (date + time) and PERIODICITY
	// text.
	@Transient
	private String period; // DAILY / WEEKLY / MONTHLY

	@Transient
	private Integer day; // WEEKLY : 1=Mon..7=Sun , MONTHLY : 1..31

	@Transient
	private Integer hour; // run hour 00-23

	@Transient
	private Integer hour1; // run minute 00-59

	public BLRS_BatchJobScheduler_Entity() {
		super();
	}

	public String getJob_id() {
		return job_id;
	}

	public void setJob_id(String job_id) {
		this.job_id = job_id;
	}

	public String getJob_name() {
		return job_name;
	}

	public void setJob_name(String job_name) {
		this.job_name = job_name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getJob_type() {
		return job_type;
	}

	public void setJob_type(String job_type) {
		this.job_type = job_type;
	}

	public String getSchm_code() {
		return schm_code;
	}

	public void setSchm_code(String schm_code) {
		this.schm_code = schm_code;
	}

	public String getReminder_type() {
		return reminder_type;
	}

	public void setReminder_type(String reminder_type) {
		this.reminder_type = reminder_type;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getPeriodicity() {
		return periodicity;
	}

	public void setPeriodicity(String periodicity) {
		this.periodicity = periodicity;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public Date getLast_run_date() {
		return last_run_date;
	}

	public void setLast_run_date(Date last_run_date) {
		this.last_run_date = last_run_date;
	}

	public Date getNext_run_date() {
		return next_run_date;
	}

	public void setNext_run_date(Date next_run_date) {
		this.next_run_date = next_run_date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getEmail_id() {
		return email_id;
	}

	public void setEmail_id(String email_id) {
		this.email_id = email_id;
	}

	public String getEmail_pwd() {
		return email_pwd;
	}

	public void setEmail_pwd(String email_pwd) {
		this.email_pwd = email_pwd;
	}

	public String getEmail_header() {
		return email_header;
	}

	public void setEmail_header(String email_header) {
		this.email_header = email_header;
	}

	public String getEmail_footer() {
		return email_footer;
	}

	public void setEmail_footer(String email_footer) {
		this.email_footer = email_footer;
	}

	public String getEntity_flg() {
		return entity_flg;
	}

	public void setEntity_flg(String entity_flg) {
		this.entity_flg = entity_flg;
	}

	public String getDel_flg() {
		return del_flg;
	}

	public void setDel_flg(String del_flg) {
		this.del_flg = del_flg;
	}

	public String getEntry_user() {
		return entry_user;
	}

	public void setEntry_user(String entry_user) {
		this.entry_user = entry_user;
	}

	public Date getEntry_time() {
		return entry_time;
	}

	public void setEntry_time(Date entry_time) {
		this.entry_time = entry_time;
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

	public String getAuth_user() {
		return auth_user;
	}

	public void setAuth_user(String auth_user) {
		this.auth_user = auth_user;
	}

	public Date getAuth_time() {
		return auth_time;
	}

	public void setAuth_time(Date auth_time) {
		this.auth_time = auth_time;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

	public Integer getHour1() {
		return hour1;
	}

	public void setHour1(Integer hour1) {
		this.hour1 = hour1;
	}
}