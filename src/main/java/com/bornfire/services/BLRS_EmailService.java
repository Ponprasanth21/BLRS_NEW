package com.bornfire.services;

import com.bornfire.entities.BLRS_EmailSmsParam_Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Sends HTML emails using the SMTP config stored in
 * Admin > Parameters > Email and SMS.
 *
 * JavaMailSender is built on-the-fly from the DB row so an Admin can change
 * host / credentials at runtime without restarting the app.
 */
@Service
public class BLRS_EmailService {

    private static final Logger log = LoggerFactory.getLogger(BLRS_EmailService.class);

    @Value("${notification.mail.connect.timeout:8000}") private String connectTimeout;
    @Value("${notification.mail.read.timeout:8000}")    private String readTimeout;
    @Value("${notification.mail.write.timeout:8000}")   private String writeTimeout;

    public JavaMailSenderImpl buildSender(BLRS_EmailSmsParam_Entity p) {
        JavaMailSenderImpl s = new JavaMailSenderImpl();
        String host = p.getEmail_host() != null ? p.getEmail_host().trim() : "";
        s.setHost(host);

        int port = 587;
        try {
            if (StringUtils.hasText(p.getEmail_port())) {
                port = Integer.parseInt(p.getEmail_port().trim());
            }
        } catch (Exception ignore) { /* keep default */ }
        s.setPort(port);

        boolean hasAuth = StringUtils.hasText(p.getEmail_pwd());
        if (hasAuth) {
            String authUser = StringUtils.hasText(p.getAuth_id()) ? p.getAuth_id().trim() : p.getSender_email().trim();
            s.setUsername(authUser);
            s.setPassword(p.getEmail_pwd().trim());
        } else if (StringUtils.hasText(p.getSender_email())) {
            s.setUsername(p.getSender_email().trim());
        }

        s.setDefaultEncoding("UTF-8");

        Properties props = s.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", hasAuth ? "true" : "false");

        if (port == 465) {
            // SMTPS / direct SSL (Gmail, Yahoo, Zoho, GoDaddy, etc.)
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.starttls.required", "false");
        } else if (port == 587) {
            // STARTTLS explicit
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.enable", "false");
        } else {
            // Standard port 25 or custom internal relay
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "false");
            props.put("mail.smtp.ssl.enable", "false");
        }

        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.connectiontimeout", connectTimeout);
        props.put("mail.smtp.timeout", readTimeout);
        props.put("mail.smtp.writetimeout", writeTimeout);
        return s;
    }

    /**
     * @return {@code null} on success, otherwise the error message.
     */
    public String send(BLRS_EmailSmsParam_Entity p,
                       String to,
                       String subject,
                       String htmlBody,
                       byte[] signaturePng,
                       String signatureCid,
                       byte[] attachment,
                       String attachmentName) {

        if (!StringUtils.hasText(to)) return "Recipient email is empty.";
        String recipient = to.trim();
        if (!recipient.contains("@")) return "Recipient email is invalid: " + recipient;

        if (!"Y".equalsIgnoreCase(p.getEmail_flg())) return "Email is disabled in Parameters.";
        if (!StringUtils.hasText(p.getEmail_host()) || !StringUtils.hasText(p.getSender_email())) {
            return "Email SMTP host and sender email must be configured.";
        }

        try {
            JavaMailSenderImpl sender = buildSender(p);
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");

            String senderEmail = p.getSender_email().trim();
            String personal = StringUtils.hasText(p.getAuth_id()) ? p.getAuth_id().trim() : "Loan Recovery System";
            h.setFrom(new InternetAddress(senderEmail, personal));

            h.setTo(recipient);
            h.setSubject(subject != null ? subject : "Loan Recovery Reminder");
            h.setText(htmlBody != null ? htmlBody : "", true);

            if (signaturePng != null && signaturePng.length > 0) {
                final String cid = (signatureCid != null && !signatureCid.isEmpty()) ? signatureCid : "lrs_signature";
                ByteArrayResource sigResource = new ByteArrayResource(signaturePng) {
                    @Override
                    public String getFilename() {
                        return "signature.png";
                    }
                };
                h.addInline(cid, sigResource, "image/png");
            }
            if (attachment != null && attachment.length > 0 && StringUtils.hasText(attachmentName)) {
                final String attName = attachmentName.trim();
                ByteArrayResource attResource = new ByteArrayResource(attachment) {
                    @Override
                    public String getFilename() {
                        return attName;
                    }
                };
                h.addAttachment(attName, attResource);
            }

            sender.send(msg);
            log.info("Email sent to {} [{}]", mask(recipient), subject);
            return null;

        } catch (Exception e) {
            String errorMsg = getRootMessage(e);
            log.error("Email to {} failed: {}", mask(recipient), errorMsg, e);
            return errorMsg;
        }
    }

    /**
     * Sends a quick test email to verify credentials and connectivity.
     */
    public String testConnection(BLRS_EmailSmsParam_Entity p, String testToEmail) {
        String to = StringUtils.hasText(testToEmail) ? testToEmail.trim() : p.getSender_email();
        if (!StringUtils.hasText(to)) {
            return "Test recipient email is required.";
        }
        String subject = "BLRS Notification System - Test Email";
        String body = "<div style=\"font-family:Arial,sans-serif;padding:15px;\">"
                + "<h3 style=\"color:#2563eb;\">Email Notification Test Successful</h3>"
                + "<p>This is a test notification from the <b>Bank Loan Recovery System (BLRS)</b>.</p>"
                + "<p>SMTP Host: <b>" + p.getEmail_host() + "</b> (Port: <b>" + p.getEmail_port() + "</b>)</p>"
                + "<p>Sender: <b>" + p.getSender_email() + "</b></p>"
                + "<p style=\"color:#64748b;font-size:12px;\">If you received this email, your email notifications are working correctly end-to-end.</p>"
                + "</div>";

        byte[] sig = (p.getSignature_img() != null && p.getSignature_img().length > 0) ? p.getSignature_img() : null;
        return send(p, to, subject, body, sig, "lrs_signature", null, null);
    }

    private String getRootMessage(Throwable t) {
        if (t == null) return "Unknown email error.";
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur.getMessage() != null ? cur.getMessage() : t.getMessage();
    }

    private String mask(String s) {
        if (s == null) return "";
        int at = s.indexOf('@');
        if (at <= 1) return "***";
        return s.charAt(0) + "***" + s.substring(at);
    }
}