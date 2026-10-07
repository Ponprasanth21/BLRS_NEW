package com.bornfire.services;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bornfire.entities.BLRS_ReminderGenerator_Entity;
import com.bornfire.entities.BLRS_ReminderGenerator_Repo;

@Service
public class BLRS_ReminderGeneratorService {

	@Autowired
	private BLRS_ReminderGenerator_Repo reminderGenRepo;

	public List<BLRS_ReminderGenerator_Entity> getReminderList() {
		return reminderGenRepo.getReminderList();
	}

	public List<BLRS_ReminderGenerator_Entity> getSelectedReminders(List<String> accts) {
		if (accts == null || accts.isEmpty()) {
			return Collections.emptyList();
		}
		return reminderGenRepo.getSelectedReminders(accts);
	}

	public String generateCsv(List<String> accts) {
		StringBuilder sb = new StringBuilder();
		sb.append("SCHEME CODE,CUSTOMER ID,ACCOUNT NUMBER,NAME OF BORROWER,OVERDUE,DEPARTMENT,REMINDER SENT\n");

		for (BLRS_ReminderGenerator_Entity r : getSelectedReminders(accts)) {
			sb.append(nz(r.getSchem_code())).append(',')
			  .append(nz(r.getCust_id())).append(',')
			  .append(nz(r.getAcct_num())).append(',')
			  .append('"').append(nz(r.getAcct_name()).replace("\"", "\"\"")).append('"').append(',')
			  .append(r.getOverdue() == null ? "0.00" : r.getOverdue().toPlainString()).append(',')
			  .append(nz(r.getDept())).append(',')
			  .append(nz(r.getRem_send())).append('\n');
		}
		return sb.toString();
	}

	private String nz(String s) {
		return s == null ? "" : s;
	}
}