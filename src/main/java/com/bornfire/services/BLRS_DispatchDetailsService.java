package com.bornfire.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_DispatchDetails_Entity;
import com.bornfire.entities.BLRS_DispatchDetails_Repo;
import com.bornfire.entities.BLRS_ReminderGenerator_Entity;

@Service
public class BLRS_DispatchDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(BLRS_DispatchDetailsService.class);

	@Autowired
	private BLRS_DispatchDetails_Repo dispatchRepo;

	@Autowired
	private com.bornfire.entities.BLRS_LoanAccountProfile_Repo loanRepo;

	public List<BLRS_DispatchDetails_Entity> getDispatchList(String reminderType, Date reminderDate) {
		Date cleanDate = truncateTime(reminderDate);
		String altType = getAlternateType(reminderType);
		return dispatchRepo.getDispatchList(reminderType, altType, cleanDate);
	}

	@Transactional
	public String saveDispatch(String reminderType, Date reminderDate, String accNum, String postalRef,
			String postalAck, String user) {
		Date cleanDate = truncateTime(reminderDate);
		String altType = getAlternateType(reminderType);
		int rows = dispatchRepo.updateDispatch(reminderType, altType, cleanDate, accNum, trim(postalRef), trim(postalAck));
		if (rows > 0 && accNum != null && !accNum.trim().isEmpty() && loanRepo != null) {
			try {
				com.bornfire.entities.BLRS_LoanAccountProfile_Entity loan = loanRepo.findByAccountNumber(accNum.trim());
				if (loan == null) {
					loan = loanRepo.findById(accNum.trim()).orElse(null);
				}
				if (loan != null) {
					if (postalRef != null) loan.setCourierPostalReference(trim(postalRef));
					if (postalAck != null) loan.setCourierPostalAcknowledgement(trim(postalAck));
					loanRepo.save(loan);
					logger.info("Synced postal details to loan account profile for account {}", accNum);
				}
			} catch (Exception e) {
				logger.warn("Could not sync postal details to loan account profile for account {}: {}", accNum, e.getMessage());
			}
		}
		logger.info("Dispatch details updated by {} for account {} ({} row)", user, accNum, rows);
		return rows > 0 ? "Data Saved Successfully" : "Record not found. Please search again.";
	}

	@Transactional
	public void recordReminderGeneration(List<BLRS_ReminderGenerator_Entity> rows, String reminderType, Date reminderDate) {
		if (rows == null || rows.isEmpty() || reminderType == null || reminderDate == null) return;
		Date cleanDate = truncateTime(reminderDate);
		String altType = getAlternateType(reminderType);
		for (BLRS_ReminderGenerator_Entity r : rows) {
			try {
				String acc = r.getAcct_num();
				if (acc == null || acc.trim().isEmpty()) continue;
				acc = acc.trim();
				BLRS_DispatchDetails_Entity existing = dispatchRepo.findByAccountAndTypeAndDate(acc, reminderType, altType, cleanDate);
				if (existing == null) {
					BLRS_DispatchDetails_Entity d = new BLRS_DispatchDetails_Entity();
					d.setReminder_type(reminderType);
					d.setRem_date(cleanDate);
					d.setAcc_num(acc);
					d.setSch_code(r.getSchem_code());
					d.setCust_id(r.getCust_id());
					d.setAcc_name(r.getAcct_name());
					d.setOverdue(r.getOverdue());
					dispatchRepo.save(d);
				}
			} catch (Exception e) {
				logger.error("Error creating dispatch record for account {}", r.getAcct_num(), e);
			}
		}
	}

	private String getAlternateType(String type) {
		if (type == null) return null;
		String digits = type.replaceAll("[^0-9]", "");
		if (digits.isEmpty()) return type;
		if (type.toLowerCase().startsWith("reminder")) {
			return "REM" + digits;
		} else if (type.toLowerCase().startsWith("rem")) {
			return "Reminder-" + digits;
		}
		return type;
	}

	private Date truncateTime(Date d) {
		if (d == null) return null;
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	private String trim(String s) {
		return s == null ? null : s.trim();
	}
}