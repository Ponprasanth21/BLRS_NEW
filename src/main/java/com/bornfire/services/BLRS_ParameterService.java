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

    @Autowired
    private BLRS_EmailService emailService;

    @Autowired
    private BLRS_SmsService smsService;

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

            db.setEmail_host(trimOrNull(form.getEmail_host()));
            db.setEmail_port(trimOrNull(form.getEmail_port()));
            db.setSender_email(trimOrNull(form.getSender_email()));
            db.setEmail_flg(yn(form.getEmail_flg()));
            db.setAuth_id(trimOrNull(form.getAuth_id()));
            db.setFooter_text(form.getFooter_text());
            db.setSms_host(trimOrNull(form.getSms_host()));
            db.setSms_user(trimOrNull(form.getSms_user()));
            db.setSms_flg(yn(form.getSms_flg()));

            // blank password = keep the saved one
            if (form.getEmail_pwd() != null && !form.getEmail_pwd().trim().isEmpty()) {
                String pwd = form.getEmail_pwd().trim();
                if (db.getEmail_host() != null && db.getEmail_host().toLowerCase().contains("gmail.com")) {
                    pwd = pwd.replaceAll("\\s+", "");
                }
                db.setEmail_pwd(pwd);
            }
            if (form.getSms_pwd() != null && !form.getSms_pwd().trim().isEmpty()) {
                db.setSms_pwd(form.getSms_pwd().trim());
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
            return "Error : could not save Email and SMS parameters: " + e.getMessage();
        }
    }

    public String testEmail(BLRS_EmailSmsParam_Entity form, String testRecipient) {
        BLRS_EmailSmsParam_Entity p = getEmailSms();
        if (form != null) {
            if (form.getEmail_host() != null && !form.getEmail_host().trim().isEmpty()) {
                p.setEmail_host(form.getEmail_host().trim());
            }
            if (form.getEmail_port() != null && !form.getEmail_port().trim().isEmpty()) {
                p.setEmail_port(form.getEmail_port().trim());
            }
            if (form.getSender_email() != null && !form.getSender_email().trim().isEmpty()) {
                p.setSender_email(form.getSender_email().trim());
            }
            if (form.getAuth_id() != null && !form.getAuth_id().trim().isEmpty()) {
                p.setAuth_id(form.getAuth_id().trim());
            }
            if (form.getEmail_pwd() != null && !form.getEmail_pwd().trim().isEmpty()) {
                String pwd = form.getEmail_pwd().trim();
                if (p.getEmail_host() != null && p.getEmail_host().toLowerCase().contains("gmail.com")) {
                    pwd = pwd.replaceAll("\\s+", "");
                }
                p.setEmail_pwd(pwd);
            }
        }
        String recipient = (testRecipient != null && !testRecipient.trim().isEmpty()) ? testRecipient.trim() : p.getSender_email();
        String err = emailService.testConnection(p, recipient);
        if (err == null) {
            return "SUCCESS: Test email sent successfully to " + recipient;
        } else {
            return "FAILED: " + err;
        }
    }

    public String testEmail(String testRecipient) {
        return testEmail(null, testRecipient);
    }

    public String testSms(String testMobile) {
        BLRS_EmailSmsParam_Entity p = getEmailSms();
        String err = smsService.testConnection(p, testMobile);
        if (err == null) {
            return "SUCCESS: Test SMS sent successfully to " + testMobile;
        } else {
            return "FAILED: " + err;
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
                String no = r.getReminder_no();                       // REM1
                String n  = no.replaceAll("[^0-9]", "");              // 1

                r.setDownload_flg(yn(first(params,
                        "download_" + no, "rem" + n + "Download")));
                r.setSignature_flg(yn(first(params,
                        "signature_" + no, "rem" + n + "Signature")));
                r.setEmail_flg(yn(first(params,
                        "email_" + no, "rem" + n + "Email")));
                r.setSms_flg(yn(first(params,
                        "sms_" + no, "rem" + n + "SMS")));

                r.setModify_user(loginUser);
                r.setModify_time(new Date());
                reminderRepo.save(r);
            }
            return "Reminder parameters saved successfully.";
        } catch (Exception e) {
            logger.error("saveReminders failed", e);
            return "Error : could not save Reminder parameters: " + e.getMessage();
        }
    }

    private String trimOrNull(String s) {
        return s != null ? s.trim() : null;
    }

    private String first(Map<String, String> m, String... keys) {
        for (String k : keys) { String v = m.get(k); if (v != null) return v; }
        return null;
    }

    private String yn(String v) {
        return "Y".equalsIgnoreCase(v) ? "Y" : "N";
    }
}