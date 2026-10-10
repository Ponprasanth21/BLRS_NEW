package com.bornfire.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_EmailSmsParam_Entity;
import com.bornfire.entities.BLRS_EmailSmsParam_Repo;
import com.bornfire.entities.BLRS_ReminderParam_Entity;
import com.bornfire.entities.BLRS_ReminderParam_Repo;

@Service
@Transactional
public class BLRS_ParameterService {

	private static final Logger logger = LoggerFactory.getLogger(BLRS_ParameterService.class);

	private static final String EMAIL_SMS_ID = "EMAILSMS";
	private static final String[] REMINDER_NOS = { "REM1", "REM2", "REM3" };

	@Autowired
	private BLRS_EmailSmsParam_Repo emailSmsRepo;

	@Autowired
	private BLRS_ReminderParam_Repo reminderRepo;

	// ---------------- Email and SMS ----------------

	public BLRS_EmailSmsParam_Entity getEmailSms() {
		return emailSmsRepo.findById(EMAIL_SMS_ID).orElseGet(() -> {
			BLRS_EmailSmsParam_Entity p = new BLRS_EmailSmsParam_Entity();
			p.setParam_id(EMAIL_SMS_ID);
			p.setEmail_flg("N");
			p.setSms_flg("N");
			return p;
		});
	}

	public String saveEmailSms(BLRS_EmailSmsParam_Entity form, byte[] signatureBytes, String signatureName,
			String loginUser) {
		try {
			BLRS_EmailSmsParam_Entity db = getEmailSms();

			db.setEmail_host(form.getEmail_host());
			db.setEmail_port(form.getEmail_port());
			db.setSender_email(form.getSender_email());
			db.setEmail_flg(yn(form.getEmail_flg()));
			db.setAuth_id(form.getAuth_id());
			db.setFooter_text(form.getFooter_text());
			db.setSms_host(form.getSms_host());
			db.setSms_user(form.getSms_user());
			db.setSms_flg(yn(form.getSms_flg()));

			// blank password = keep the saved one
			if (form.getEmail_pwd() != null && !form.getEmail_pwd().trim().isEmpty()) {
				db.setEmail_pwd(form.getEmail_pwd());
			}
			if (form.getSms_pwd() != null && !form.getSms_pwd().trim().isEmpty()) {
				db.setSms_pwd(form.getSms_pwd());
			}
			if (signatureBytes != null && signatureBytes.length > 0) {
				db.setSignature_img(signatureBytes);
				db.setSignature_name(signatureName);
			}

			db.setModify_user(loginUser);
			db.setModify_time(new Date());
			emailSmsRepo.save(db);
			return "Email and SMS parameters saved successfully.";
		} catch (Exception e) {
			logger.error("saveEmailSms failed", e);
			return "Error : could not save Email and SMS parameters.";
		}
	}

	// ---------------- Reminder ----------------

	public List<BLRS_ReminderParam_Entity> getReminders() {
		List<BLRS_ReminderParam_Entity> list = new ArrayList<>();
		for (String no : REMINDER_NOS) {
			BLRS_ReminderParam_Entity r = reminderRepo.findById(no).orElseGet(() -> {
				BLRS_ReminderParam_Entity n = new BLRS_ReminderParam_Entity();
				n.setReminder_no(no);
				n.setDownload_flg("N");
				n.setSignature_flg("N");
				n.setEmail_flg("N");
				n.setSms_flg("N");
				return n;
			});
			list.add(r);
		}
		return list;
	}

	public String saveReminders(Map<String, String> params, String loginUser) {
		try {
			for (BLRS_ReminderParam_Entity r : getReminders()) {
				String no = r.getReminder_no();
				r.setDownload_flg(yn(params.get("download_" + no)));
				r.setSignature_flg(yn(params.get("signature_" + no)));
				r.setEmail_flg(yn(params.get("email_" + no)));
				r.setSms_flg(yn(params.get("sms_" + no)));
				r.setModify_user(loginUser);
				r.setModify_time(new Date());
				reminderRepo.save(r);
			}
			return "Reminder parameters saved successfully.";
		} catch (Exception e) {
			logger.error("saveReminders failed", e);
			return "Error : could not save Reminder parameters.";
		}
	}

	private String yn(String v) {
		return "Y".equalsIgnoreCase(v) ? "Y" : "N";
	}
}