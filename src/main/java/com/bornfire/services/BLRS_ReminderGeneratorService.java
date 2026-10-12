package com.bornfire.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bornfire.entities.BLRS_ReminderGenerator_Entity;
import com.bornfire.entities.BLRS_ReminderGenerator_Repo;

@Service
public class BLRS_ReminderGeneratorService {

	/** Oracle allows max 1000 items in an IN list (ORA-01795) */
	private static final int CHUNK_SIZE = 900;

	@Autowired
	private BLRS_ReminderGenerator_Repo reminderGenRepo;

	public List<BLRS_ReminderGenerator_Entity> getReminderList() {
		return reminderGenRepo.getReminderList();
	}

	public List<BLRS_ReminderGenerator_Entity> getSelectedReminders(List<String> accts) {
		if (accts == null || accts.isEmpty()) {
			return Collections.emptyList();
		}
		List<BLRS_ReminderGenerator_Entity> result = new ArrayList<>();
		for (int i = 0; i < accts.size(); i += CHUNK_SIZE) {
			List<String> chunk = accts.subList(i, Math.min(i + CHUNK_SIZE, accts.size()));
			result.addAll(reminderGenRepo.getSelectedReminders(chunk));
		}
		return result;
	}

	/** Builds the CSV and packs it into a ZIP (manual 4.1: system downloads a .zip file). */
	public byte[] generateZip(List<String> accts, String reminderType, String email, String sms)
			throws IOException {
		String csv = generateCsv(accts, reminderType, email, sms);
		String stamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(bos)) {
			zos.putNextEntry(new ZipEntry("reminders_" + stamp + ".csv"));
			zos.write(csv.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}
		return bos.toByteArray();
	}

	public String generateCsv(List<String> accts, String reminderType, String email, String sms) {
		StringBuilder sb = new StringBuilder();
		sb.append("SCHEME CODE,CUSTOMER ID,ACCOUNT NUMBER,NAME OF BORROWER,OVERDUE,DEPARTMENT,")
		  .append("REMINDER SENT,REMINDER TYPE,EMAIL,SMS\n");

		for (BLRS_ReminderGenerator_Entity r : getSelectedReminders(accts)) {
			sb.append(nz(r.getSchem_code())).append(',')
			  .append(nz(r.getCust_id())).append(',')
			  .append(nz(r.getAcct_num())).append(',')
			  .append('"').append(safe(r.getAcct_name())).append('"').append(',')
			  .append(r.getOverdue() == null ? "0.00" : r.getOverdue().toPlainString()).append(',')
			  .append(nz(r.getDept())).append(',')
			  .append(nz(r.getRem_send())).append(',')
			  .append(nz(reminderType)).append(',')
			  .append(nz(email)).append(',')
			  .append(nz(sms)).append('\n');
		}
		return sb.toString();
	}

	/** Escapes quotes and blocks Excel formula injection (=, +, -, @ at the start). */
	private String safe(String s) {
		String v = nz(s);
		if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
			v = "'" + v;
		}
		return v.replace("\"", "\"\"");
	}

	private String nz(String s) {
		return s == null ? "" : s;
	}
}