package com.bornfire.services;

import com.bornfire.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Enforces the 3-gate rule:
 *
 *   Send = GlobalGate(Parameters>EmailSMS)  AND
 *          TypeGate  (Parameters>Reminder)  AND
 *          UserGate  (checkbox / option on Reminder Generation or Batch Job)
 */
@Service
public class BLRS_NotificationService {

    private static final Logger log = LoggerFactory.getLogger(BLRS_NotificationService.class);
    private static final String SIGNATURE_CID = "lrs_signature";

    @Autowired private BLRS_EmailService             emailService;
    @Autowired private BLRS_SmsService               smsService;
    @Autowired private BLRS_NotificationLog_Repo     logRepo;
    @Autowired private BLRS_LoanAccountProfile_Repo  loanRepo;
    @Autowired private BLRS_ReminderGenerator_Repo   reminderGenRepo;

    /* ---------------------------------------------------------
     *  Public entry point
     * --------------------------------------------------------- */
    @Async("notificationExecutor")
    public void dispatchReminders(BLRS_EmailSmsParam_Entity emailSms,
                                  Map<String, String> reminderOption,
                                  List<BLRS_ReminderGenerator_Entity> rows,
                                  boolean userWantsEmail,
                                  boolean userWantsSms,
                                  String loginUser) {

        if (emailSms == null || reminderOption == null || rows == null || rows.isEmpty()) {
            log.warn("Notification dispatch skipped: missing required parameters or empty rows.");
            return;
        }

        // ---------- 3 GATES ----------
        boolean globalEmail = "Y".equalsIgnoreCase(emailSms.getEmail_flg());
        boolean globalSms   = "Y".equalsIgnoreCase(emailSms.getSms_flg());
        boolean typeEmail   = "Y".equalsIgnoreCase(reminderOption.get("email"));
        boolean typeSms     = "Y".equalsIgnoreCase(reminderOption.get("sms"));
        boolean signOn      = "Y".equalsIgnoreCase(reminderOption.get("signature"));

        boolean doEmail = globalEmail && typeEmail && userWantsEmail;
        boolean doSms   = globalSms   && typeSms   && userWantsSms;

        log.info("Notification dispatch : type={}, accounts={}, globalE={}, globalS={}, "
                        + "typeE={}, typeS={}, userE={}, userS={} -> doEmail={}, doSms={}",
                reminderOption.get("name"), rows.size(),
                globalEmail, globalSms, typeEmail, typeSms, userWantsEmail, userWantsSms,
                doEmail, doSms);

        if (!doEmail && !doSms) return;

        for (BLRS_ReminderGenerator_Entity r : rows) {
            try {
                // Find borrower profile using customerId or account number fallback
                BLRS_LoanAccountProfile_Entity loan = null;
                if (StringUtils.hasText(r.getCust_id())) {
                    loan = loanRepo.findByCustomerId(r.getCust_id().trim());
                }
                if (loan == null && StringUtils.hasText(r.getAcct_num())) {
                    loan = loanRepo.findFirstByAccountNumber(r.getAcct_num().trim());
                }
                if (loan == null && StringUtils.hasText(r.getAcc_number())) {
                    loan = loanRepo.findFirstByAccountNumber(r.getAcc_number().trim());
                }

                String email  = (loan != null && StringUtils.hasText(loan.getEmailId())) ? loan.getEmailId().trim() : null;
                String mobile = (loan != null && StringUtils.hasText(loan.getMobileNo())) ? loan.getMobileNo().trim() : null;

                // Fallbacks for account name / overdue
                if (!StringUtils.hasText(r.getAcct_name()) && loan != null && StringUtils.hasText(loan.getBorrowerName())) {
                    r.setAcct_name(loan.getBorrowerName());
                }
                if (r.getOverdue() == null && loan != null) {
                    if (loan.getOverdueLoanAmount() != null) {
                        r.setOverdue(loan.getOverdueLoanAmount());
                    } else if (loan.getLoanCapitalArrears() != null) {
                        r.setOverdue(loan.getLoanCapitalArrears());
                    }
                }

                boolean emailSent = false;
                boolean smsSent   = false;

                if (doEmail) {
                    if (StringUtils.hasText(email)) {
                        emailSent = sendEmail(emailSms, reminderOption, r, email, signOn, loginUser);
                    } else {
                        writeAudit("EMAIL", reminderOption.get("name"), r.getAcct_num(), r.getCust_id(),
                                null, "Recipient email not found in loan profile.", "Recipient email not found in loan profile.", null, loginUser);
                    }
                }

                if (doSms) {
                    if (StringUtils.hasText(mobile)) {
                        smsSent = sendSms(emailSms, reminderOption, r, mobile, loginUser);
                    } else {
                        writeAudit("SMS", reminderOption.get("name"), r.getAcct_num(), r.getCust_id(),
                                null, "Recipient mobile not found in loan profile.", "Recipient mobile not found in loan profile.", null, loginUser);
                    }
                }

                if (emailSent || smsSent) {
                    r.setRem_send("Y");
                    if (reminderOption.get("name") != null) {
                        r.setRem_type(reminderOption.get("name"));
                    }
                    try {
                        reminderGenRepo.save(r);
                    } catch (Exception re) {
                        log.warn("Could not update rem_send flag on entity {}", r.getAcct_num(), re);
                    }
                }

            } catch (Exception e) {
                log.error("Notification loop error for account {}", r.getAcct_num(), e);
            }
        }
    }

    /* ---------------------------------------------------------
     *  Email
     * --------------------------------------------------------- */
    private boolean sendEmail(BLRS_EmailSmsParam_Entity emailSms,
                              Map<String, String> opt,
                              BLRS_ReminderGenerator_Entity r,
                              String to,
                              boolean withSignature,
                              String loginUser) {

        byte[] sigBytes = (withSignature && emailSms.getSignature_img() != null && emailSms.getSignature_img().length > 0)
                ? emailSms.getSignature_img()
                : null;
        boolean hasActualSignature = (sigBytes != null && sigBytes.length > 0);

        String subject = buildEmailSubject(opt.get("name"), nz(r.getAcct_num()));
        String html    = buildEmailHtml(r.getAcct_name(), r.getAcct_num(),
                r.getSchem_code(), r.getOverdue(),
                emailSms.getFooter_text(), hasActualSignature);

        String err = emailService.send(emailSms, to, subject, html,
                sigBytes, SIGNATURE_CID, null, null);

        writeAudit("EMAIL", opt.get("name"), r.getAcct_num(), r.getCust_id(),
                to, subject, err, null, loginUser);

        return err == null;
    }

    /* ---------------------------------------------------------
     *  SMS
     * --------------------------------------------------------- */
    private boolean sendSms(BLRS_EmailSmsParam_Entity emailSms,
                            Map<String, String> opt,
                            BLRS_ReminderGenerator_Entity r,
                            String mobile,
                            String loginUser) {

        String text = buildSmsText(r.getAcct_name(), r.getAcct_num(),
                r.getSchem_code(), r.getOverdue());

        String err = smsService.send(emailSms, mobile, text);

        writeAudit("SMS", opt.get("name"), r.getAcct_num(), r.getCust_id(),
                mobile, text, err, null, loginUser);

        return err == null;
    }

    /* ---------------------------------------------------------
     *  Templates
     * --------------------------------------------------------- */
    private String buildEmailSubject(String reminderName, String accountNo) {
        return "Loan Recovery Reminder \u2013 Account " + safe(accountNo)
                + " (" + safe(reminderName) + ")";
    }

    private String buildEmailHtml(String customerName, String accountNo, String scheme,
                                  BigDecimal overdue, String footer, boolean hasActualSignature) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:Segoe UI,Arial,sans-serif;font-size:14px;color:#1e293b;line-height:1.6;\">");
        sb.append("<p>Dear <b>").append(safe(customerName)).append("</b>,</p>");
        sb.append("<p>Our records show an outstanding overdue on your loan account ")
          .append("<b>").append(safe(accountNo)).append("</b>");
        if (StringUtils.hasText(scheme)) {
            sb.append(" (Scheme ").append(safe(scheme)).append(")");
        }
        if (overdue != null) {
            sb.append(" of <b>Rs. ").append(overdue.toPlainString()).append("</b>");
        }
        sb.append(". Kindly arrange payment at the earliest to avoid further action.</p>");

        if (hasActualSignature) {
            sb.append("<p>Regards,<br/><img src=\"cid:").append(SIGNATURE_CID)
              .append("\" alt=\"Authorized Signature\" style=\"max-height:70px;\"/></p>");
        } else {
            sb.append("<p>Regards,<br/><b>Loan Recovery Team</b></p>");
        }
        if (StringUtils.hasText(footer)) {
            sb.append("<hr style=\"border:none;border-top:1px solid #cbd5e1;margin-top:20px;\"/>")
              .append("<div style=\"font-size:12px;color:#64748b;\">")
              .append(safe(footer)).append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String buildSmsText(String customerName, String accountNo, String scheme,
                                BigDecimal overdue) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(safe(customerName))
          .append(", your loan account ").append(safe(accountNo));
        if (StringUtils.hasText(scheme)) sb.append(" (").append(scheme).append(")");
        sb.append(" has overdue ");
        if (overdue != null) sb.append("Rs. ").append(overdue.toPlainString()).append(". ");
        sb.append("Kindly pay to avoid further action. - LRS");
        return sb.toString();
    }

    /* ---------------------------------------------------------
     *  Audit
     * --------------------------------------------------------- */
    @Transactional
    public void writeAudit(String channel, String reminderNo, String accountNo,
                           String customerId, String recipient, String subject,
                           String err, String attachmentName, String loginUser) {
        try {
            BLRS_NotificationLog_Entity row = new BLRS_NotificationLog_Entity();
            row.setChannel(truncate(channel, 10));
            row.setReminderNo(truncate(reminderNo, 10));
            row.setAccountNo(truncate(accountNo, 30));
            row.setCustomerId(truncate(customerId, 30));
            row.setRecipient(truncate(recipient, 200));
            row.setSubject(truncate(subject, 500));
            row.setStatus(truncate(err == null ? "SENT" : "FAILED", 15));
            row.setErrorMsg(truncate(err, 500));
            row.setAttachmentName(truncate(attachmentName, 200));
            row.setSentTime(new Date());
            row.setEntryUser(truncate(loginUser, 30));
            logRepo.save(row);
        } catch (Exception e) {
            log.error("Audit write failed for {} / {}", accountNo, channel, e);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
    private String safe(String s) { return s == null ? "" : s; }
    private String nz(String s)   { return s == null ? "" : s; }
}