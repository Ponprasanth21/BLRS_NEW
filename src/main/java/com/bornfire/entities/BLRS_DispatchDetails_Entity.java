package com.bornfire.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Dispatch Details (Operation -> Dispatch Details). NOTE: table name is an
 * ASSUMPTION - change it to your real table.
 */
@Entity
@Table(name = "BLRS_DISPATCH_DETAILS_TAB")
@IdClass(BLRS_DispatchDetails_Entity.PK.class)
public class BLRS_DispatchDetails_Entity {

	@Id
	@Column(name = "REMINDER_TYPE")
	private String reminder_type;

	@Id
	@Column(name = "REM_DATE")
	@Temporal(TemporalType.DATE)
	private Date rem_date;

	@Id
	@Column(name = "ACC_NUM")
	private String acc_num;

	@Column(name = "SCH_CODE")
	private String sch_code;

	@Column(name = "CUST_ID")
	private String cust_id;

	@Column(name = "ACC_NAME")
	private String acc_name;

	@Column(name = "OVERDUE")
	private BigDecimal overdue;

	@Column(name = "POSTAL_REF")
	private String postal_ref;

	@Column(name = "POSTAL_ACK")
	private String postal_ack;

	public BLRS_DispatchDetails_Entity() {
		super();
	}

	/** Composite primary key */
	public static class PK implements Serializable {
		private static final long serialVersionUID = 1L;
		private String reminder_type;
		private Date rem_date;
		private String acc_num;

		public PK() {
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof PK))
				return false;
			PK p = (PK) o;
			return Objects.equals(reminder_type, p.reminder_type) && Objects.equals(rem_date, p.rem_date)
					&& Objects.equals(acc_num, p.acc_num);
		}

		@Override
		public int hashCode() {
			return Objects.hash(reminder_type, rem_date, acc_num);
		}
	}

	public String getReminder_type() {
		return reminder_type;
	}

	public void setReminder_type(String v) {
		this.reminder_type = v;
	}

	public Date getRem_date() {
		return rem_date;
	}

	public void setRem_date(Date v) {
		this.rem_date = v;
	}

	public String getAcc_num() {
		return acc_num;
	}

	public void setAcc_num(String v) {
		this.acc_num = v;
	}

	public String getSch_code() {
		return sch_code;
	}

	public void setSch_code(String v) {
		this.sch_code = v;
	}

	public String getCust_id() {
		return cust_id;
	}

	public void setCust_id(String v) {
		this.cust_id = v;
	}

	public String getAcc_name() {
		return acc_name;
	}

	public void setAcc_name(String v) {
		this.acc_name = v;
	}

	public BigDecimal getOverdue() {
		return overdue;
	}

	public void setOverdue(BigDecimal v) {
		this.overdue = v;
	}

	public String getPostal_ref() {
		return postal_ref;
	}

	public void setPostal_ref(String v) {
		this.postal_ref = v;
	}

	public String getPostal_ack() {
		return postal_ack;
	}

	public void setPostal_ack(String v) {
		this.postal_ack = v;
	}
}