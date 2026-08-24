package com.bornfire.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="BGLS_CONTROL_TABLE")
public class BGLS_Control_Table {

	private String	org_name;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Id
	private Date	tran_date;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date	next_work_date;
	private String	journal_cons;
	private String	int_oper;
	private String	sc_oper;
	private String	bj_oper;
	private String	ledger_cons;
	private String	acct_cons;
	private String	hol_check;
	private String	mov_dac;
	private String	mov_journal;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date	dcp_start_time;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date	dcp_end_time;
	private String	dcp_user;
	private String	dcp_flg;
	private String	dcp_status;
	private String gl_con;
	private Date  previous_date;
	
	
	public String getGl_con() {
		return gl_con;
	}
	public void setGl_con(String gl_con) {
		this.gl_con = gl_con;
	}
	public Date getPrevious_date() {
		return previous_date;
	}
	public void setPrevious_date(Date previous_date) {
		this.previous_date = previous_date;
	}
	public String getOrg_name() {
		return org_name;
	}
	public void setOrg_name(String org_name) {
		this.org_name = org_name;
	}
	public Date getTran_date() {
		return tran_date;
	}
	public void setTran_date(Date tran_date) {
		this.tran_date = tran_date;
	}
	public Date getNext_work_date() {
		return next_work_date;
	}
	public void setNext_work_date(Date next_work_date) {
		this.next_work_date = next_work_date;
	}
	public String getJournal_cons() {
		return journal_cons;
	}
	public void setJournal_cons(String journal_cons) {
		this.journal_cons = journal_cons;
	}
	public String getInt_oper() {
		return int_oper;
	}
	public void setInt_oper(String int_oper) {
		this.int_oper = int_oper;
	}
	public String getSc_oper() {
		return sc_oper;
	}
	public void setSc_oper(String sc_oper) {
		this.sc_oper = sc_oper;
	}
	public String getBj_oper() {
		return bj_oper;
	}
	public void setBj_oper(String bj_oper) {
		this.bj_oper = bj_oper;
	}
	public String getLedger_cons() {
		return ledger_cons;
	}
	public void setLedger_cons(String ledger_cons) {
		this.ledger_cons = ledger_cons;
	}
	public String getAcct_cons() {
		return acct_cons;
	}
	public void setAcct_cons(String acct_cons) {
		this.acct_cons = acct_cons;
	}
	public String getHol_check() {
		return hol_check;
	}
	public void setHol_check(String hol_check) {
		this.hol_check = hol_check;
	}
	public String getMov_dac() {
		return mov_dac;
	}
	public void setMov_dac(String mov_dac) {
		this.mov_dac = mov_dac;
	}
	public String getMov_journal() {
		return mov_journal;
	}
	public void setMov_journal(String mov_journal) {
		this.mov_journal = mov_journal;
	}
	public Date getDcp_start_time() {
		return dcp_start_time;
	}
	public void setDcp_start_time(Date dcp_start_time) {
		this.dcp_start_time = dcp_start_time;
	}
	public Date getDcp_end_time() {
		return dcp_end_time;
	}
	public void setDcp_end_time(Date dcp_end_time) {
		this.dcp_end_time = dcp_end_time;
	}
	public String getDcp_user() {
		return dcp_user;
	}
	public void setDcp_user(String dcp_user) {
		this.dcp_user = dcp_user;
	}
	public String getDcp_flg() {
		return dcp_flg;
	}
	public void setDcp_flg(String dcp_flg) {
		this.dcp_flg = dcp_flg;
	}
	public String getDcp_status() {
		return dcp_status;
	}
	public void setDcp_status(String dcp_status) {
		this.dcp_status = dcp_status;
	}
	public BGLS_Control_Table() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
