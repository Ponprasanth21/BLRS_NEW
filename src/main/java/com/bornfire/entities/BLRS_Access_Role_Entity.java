package com.bornfire.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BLRS_ACCESS_ROLE_TABLE")
public class BLRS_Access_Role_Entity {

	@Id
	private String role_id;
	private String role_desc;
	private String permissions;
	private String work_class;
	private String domain_id;
	private String admin;
	private String entity_flg;
	private String auth_flg;
	private String modify_flg;
	private String del_flg;
	private String menulist;
	private String entry_user;
	private String modify_user;
	private String auth_user;
	private Date entry_time;
	private Date modify_time;
	private Date auth_time;
	private String audit_logs;
	private String operations;
	private String inquiries;
	private String reports;
	private String audit_operations;
	private String ips_operations;
	private String monitoring;
	private String myt_registration;
	private String wallet_master;
	private String consent_registration;
	private String merchant_registration;
	private String new_role_flg;
	private String remarks;

	public String getRole_id() {
		return role_id;
	}

	public void setRole_id(String role_id) {
		this.role_id = role_id;
	}

	public String getRole_desc() {
		return role_desc;
	}

	public void setRole_desc(String role_desc) {
		this.role_desc = role_desc;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	public String getWork_class() {
		return work_class;
	}

	public void setWork_class(String work_class) {
		this.work_class = work_class;
	}

	public String getDomain_id() {
		return domain_id;
	}

	public void setDomain_id(String domain_id) {
		this.domain_id = domain_id;
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
	}

	public String getEntity_flg() {
		return entity_flg;
	}

	public void setEntity_flg(String entity_flg) {
		this.entity_flg = entity_flg;
	}

	public String getAuth_flg() {
		return auth_flg;
	}

	public void setAuth_flg(String auth_flg) {
		this.auth_flg = auth_flg;
	}

	public String getModify_flg() {
		return modify_flg;
	}

	public void setModify_flg(String modify_flg) {
		this.modify_flg = modify_flg;
	}

	public String getDel_flg() {
		return del_flg;
	}

	public void setDel_flg(String del_flg) {
		this.del_flg = del_flg;
	}

	public String getMenulist() {
		return menulist;
	}

	public void setMenulist(String menulist) {
		this.menulist = menulist;
	}

	public String getEntry_user() {
		return entry_user;
	}

	public void setEntry_user(String entry_user) {
		this.entry_user = entry_user;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
	}

	public String getAuth_user() {
		return auth_user;
	}

	public void setAuth_user(String auth_user) {
		this.auth_user = auth_user;
	}

	public Date getEntry_time() {
		return entry_time;
	}

	public void setEntry_time(Date entry_time) {
		this.entry_time = entry_time;
	}

	public Date getModify_time() {
		return modify_time;
	}

	public void setModify_time(Date modify_time) {
		this.modify_time = modify_time;
	}

	public Date getAuth_time() {
		return auth_time;
	}

	public void setAuth_time(Date auth_time) {
		this.auth_time = auth_time;
	}

	public String getAudit_logs() {
		return audit_logs;
	}

	public void setAudit_logs(String audit_logs) {
		this.audit_logs = audit_logs;
		this.audit_operations = audit_logs;
	}

	public String getOperations() {
		return operations;
	}

	public void setOperations(String operations) {
		this.operations = operations;
		this.ips_operations = operations;
	}

	public String getInquiries() {
		return inquiries;
	}

	public void setInquiries(String inquiries) {
		this.inquiries = inquiries;
		this.monitoring = inquiries;
	}

	public String getReports() {
		return reports;
	}

	public void setReports(String reports) {
		this.reports = reports;
		this.myt_registration = reports;
	}

	public String getAudit_operations() {
		return audit_operations != null ? audit_operations : audit_logs;
	}

	public void setAudit_operations(String audit_operations) {
		this.audit_operations = audit_operations;
		this.audit_logs = audit_operations;
	}

	public String getIps_operations() {
		return ips_operations != null ? ips_operations : operations;
	}

	public void setIps_operations(String ips_operations) {
		this.ips_operations = ips_operations;
		this.operations = ips_operations;
	}

	public String getMonitoring() {
		return monitoring != null ? monitoring : inquiries;
	}

	public void setMonitoring(String monitoring) {
		this.monitoring = monitoring;
		this.inquiries = monitoring;
	}

	public String getMyt_registration() {
		return myt_registration != null ? myt_registration : reports;
	}

	public void setMyt_registration(String myt_registration) {
		this.myt_registration = myt_registration;
		this.reports = myt_registration;
	}

	public String getWallet_master() {
		return wallet_master;
	}

	public void setWallet_master(String wallet_master) {
		this.wallet_master = wallet_master;
	}

	public String getConsent_registration() {
		return consent_registration;
	}

	public void setConsent_registration(String consent_registration) {
		this.consent_registration = consent_registration;
	}

	public String getMerchant_registration() {
		return merchant_registration;
	}

	public void setMerchant_registration(String merchant_registration) {
		this.merchant_registration = merchant_registration;
	}

	public String getNew_role_flg() {
		return new_role_flg;
	}

	public void setNew_role_flg(String new_role_flg) {
		this.new_role_flg = new_role_flg;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public BLRS_Access_Role_Entity() {
		super();
	}
}