package com.bornfire.services;

import com.bornfire.entities.BLRS_EmailSmsParam_Entity;
import com.twilio.exception.ApiException;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Dedicated Twilio SMS Service for Bank Loan Recovery System (BLRS).
 *
 * Sends SMS notifications using the official Twilio Java SDK.
 * Configured via environment variables (TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_PHONE_NUMBER)
 * with graceful fallback to application properties and parameter table settings.
 * All phone numbers are strictly formatted to E.164 standard.
 * Credentials and sensitive phone details are protected and never logged in plain text.
 */
@Service
public class BLRS_SmsService {

    private static final Logger log = LoggerFactory.getLogger(BLRS_SmsService.class);

    @Value("${twilio.account-sid:${TWILIO_ACCOUNT_SID:}}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:${TWILIO_AUTH_TOKEN:}}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number:${TWILIO_PHONE_NUMBER:}}")
    private String twilioPhoneNumber;

    @Value("${twilio.default-country-code:+91}")
    private String defaultCountryCode;

    @Value("${notification.sms.timeout:8000}")
    private int timeoutMs;

    // Optional client holder for mocking in automated unit tests
    private TwilioRestClient customRestClient;

    public void setCustomRestClient(TwilioRestClient client) {
        this.customRestClient = client;
    }

    /**
     * Sends SMS via Twilio using parameters from BLRS_EmailSmsParam_Entity or environment configuration.
     *
     * @param p BLRS_EmailSmsParam_Entity containing system-level flags
     * @param mobile Customer mobile number
     * @param text SMS body text
     * @return null on success, or a safe error message on failure
     */
    public String send(BLRS_EmailSmsParam_Entity p, String mobile, String text) {
        if (p != null && "N".equalsIgnoreCase(p.getSms_flg())) {
            log.info("SMS dispatch skipped: SMS is disabled in system parameters.");
            return "SMS is disabled in Parameters.";
        }
        return sendSms(mobile, text, p);
    }

    /**
     * Direct SMS dispatch using configured Twilio credentials.
     *
     * @param mobile Customer phone number
     * @param text SMS message body
     * @return null on success, or safe error string on failure
     */
    public String sendSms(String mobile, String text) {
        return sendSms(mobile, text, null);
    }

    /**
     * Core dispatch logic using Twilio Java SDK.
     */
    public String sendSms(String mobile, String text, BLRS_EmailSmsParam_Entity p) {
        if (!StringUtils.hasText(mobile)) {
            return "Recipient mobile is empty.";
        }

        String e164Mobile = formatToE164(mobile);
        if (e164Mobile == null) {
            log.warn("SMS rejected: Invalid recipient phone format '{}'", mask(mobile));
            return "Invalid recipient mobile number (must be valid E.164 format e.g. +91XXXXXXXXXX): " + mask(mobile);
        }

        String sid = resolveAccountSid(p);
        String token = resolveAuthToken(p);
        String fromPhone = resolveFromPhone(p);

        String messageBody = text != null ? text.trim() : "";
        if (messageBody.isEmpty()) {
            return "SMS message body cannot be empty.";
        }

        // 1) 100% Free Demo Mode (Zero cost, instant simulation for testing & project review)
        if (sid != null && ("DEMO".equalsIgnoreCase(sid) || "SIMULATOR".equalsIgnoreCase(sid) || "TEST".equalsIgnoreCase(sid))) {
            log.info("[BLRS DEMO GATEWAY] SMS simulated successfully to {}: \"{}\"", mask(e164Mobile), messageBody);
            return null; // success
        }

        // 2) Fast2SMS Free Gateway (Direct Indian SMS without Twilio US number or payment)
        if (sid != null && (sid.startsWith("F2S_") || "FAST2SMS".equalsIgnoreCase(token))) {
            return sendFast2Sms(sid, e164Mobile, messageBody);
        }

        if (!StringUtils.hasText(sid) || !StringUtils.hasText(token)) {
            log.error("Twilio credentials missing. Please set TWILIO_ACCOUNT_SID and TWILIO_AUTH_TOKEN (or enter 'DEMO' for Free Demo Mode).");
            return "Twilio SMS credentials not configured. Please check environment variables or Admin > Parameters (or enter 'DEMO' in Account SID for Free Demo Mode).";
        }

        if (!isValidAccountSid(sid)) {
            log.error("Invalid Twilio Account SID format: '{}'", mask(sid));
            return "Invalid Twilio Account SID. The SID must start with 'AC' and be 34 characters long (or enter 'DEMO' in Account SID for Free Demo Mode).";
        }

        if (!StringUtils.hasText(fromPhone)) {
            log.error("Twilio sender phone number missing. Please set TWILIO_PHONE_NUMBER.");
            return "Twilio sender phone number not configured. Please enter Twilio phone number in Admin > Parameters (or enter 'DEMO' in Account SID for Free Demo Mode).";
        }

        String e164From = formatToE164(fromPhone);
        if (e164From == null) {
            log.error("Configured Twilio sender phone number '{}' is not valid E.164", mask(fromPhone));
            return "Configured Twilio sender phone number is invalid: " + mask(fromPhone);
        }

        try {
            TwilioRestClient client = getOrCreateClient(sid, token);
            Message message = Message.creator(
                    new PhoneNumber(e164Mobile),
                    new PhoneNumber(e164From),
                    messageBody
            ).create(client);

            String status = message.getStatus() != null ? message.getStatus().toString() : "accepted";
            log.info("Twilio SMS dispatched to {} [SID: {}, Status: {}, Direction: {}]",
                    mask(e164Mobile), message.getSid(), status, message.getDirection());

            if (message.getErrorCode() != null && message.getErrorCode() > 0) {
                log.error("Twilio returned message error: code={}, message={}", message.getErrorCode(), message.getErrorMessage());
                return "Twilio message failed [code=" + message.getErrorCode() + "]: " + message.getErrorMessage();
            }

            return null; // success

        } catch (ApiException ae) {
            log.error("Twilio API error sending SMS to {}: code={}, message={}",
                    mask(e164Mobile), ae.getCode(), ae.getMessage());
            if (ae.getCode() != null) {
                if (ae.getCode() == 20003) {
                    return "Twilio API error [code=20003]: Authentication Error - invalid username. Please verify that your 'Account SID' (starts with AC...) and 'Auth Token' in Admin > Parameters match your Twilio Console credentials.";
                } else if (ae.getCode() == 572002 || ae.getCode() == 21608) {
                    return "Twilio Trial Restriction [code=" + ae.getCode() + "]: " + ae.getMessage() + "\n\n"
                            + "Fix: Twilio Free Trial accounts can only send SMS to numbers verified on your account.\n"
                            + "1. Go to Twilio Console: https://console.twilio.com\n"
                            + "2. Navigate to 'Phone Numbers' > 'Manage' > 'Verified Caller IDs'\n"
                            + "3. Add and verify your mobile number (" + mask(e164Mobile) + ") with the OTP code\n"
                            + "4. Alternatively, upgrade your Twilio account to send SMS to any customer.";
                } else if (ae.getCode() == 21408) {
                    return "Twilio Geo-Permission Error [code=21408]: SMS to this destination country is disabled. "
                            + "Enable the country in Twilio Console > Messaging > Settings > Geo-permissions.";
                }
            }
            return "Twilio API error [code=" + ae.getCode() + "]: " + ae.getMessage();
        } catch (Exception e) {
            log.error("Unexpected error sending Twilio SMS to {}: {}", mask(e164Mobile), e.getMessage(), e);
            return "SMS dispatch failed: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    /**
     * Tests SMS connection by dispatching a loan overdue and due amount alert.
     * Always allows test dispatch regardless of production sms_flg.
     */
    public String testConnection(BLRS_EmailSmsParam_Entity p, String testMobile) {
        return testConnection(p, testMobile, null);
    }

    /**
     * Tests SMS connection with custom or standard loan overdue and due amount message.
     */
    public String testConnection(BLRS_EmailSmsParam_Entity p, String testMobile, String customText) {
        if (!StringUtils.hasText(testMobile)) {
            return "Test mobile number is required.";
        }
        String messageBody = StringUtils.hasText(customText)
                ? customText.trim()
                : "BLRS Alert: Dear Customer, your loan account has overdue amount of Rs. 25,000.00 and total due balance of Rs. 1,50,000.00. Kindly arrange payment immediately to avoid further recovery action. - BLRS";
        return sendSms(testMobile, messageBody, p);
    }

    /**
     * Checks if the given Twilio Account SID format is valid (starts with AC or SK, 34 alphanumeric chars,
     * or 'DEMO' for Free Simulation Mode, or 'F2S_' for Fast2SMS Gateway).
     */
    public boolean isValidAccountSid(String sid) {
        if (!StringUtils.hasText(sid)) {
            return false;
        }
        String s = sid.trim();
        if ("DEMO".equalsIgnoreCase(s) || "SIMULATOR".equalsIgnoreCase(s) || "TEST".equalsIgnoreCase(s) || s.startsWith("F2S_")) {
            return true;
        }
        return s.matches("^(AC|SK)[a-zA-Z0-9]{30,34}$");
    }

    /**
     * Fast2SMS Free Indian SMS Gateway integration (requires no US number, no credit card).
     */
    private String sendFast2Sms(String apiKey, String e164Mobile, String message) {
        try {
            String localNumber = e164Mobile.startsWith("+91") ? e164Mobile.substring(3) : e164Mobile;
            String cleanKey = apiKey.startsWith("F2S_") ? apiKey.substring(4) : apiKey;

            java.net.URL url = new java.net.URL("https://www.fast2sms.com/dev/bulkV2");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("authorization", cleanKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);

            String escapedMsg = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(message);
            String jsonPayload = "{\"route\":\"q\",\"message\":" + escapedMsg + ",\"numbers\":\"" + localNumber + "\"}";

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                log.info("Fast2SMS successfully dispatched to {}", mask(e164Mobile));
                return null;
            } else {
                java.io.InputStream es = conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();
                String errResp = org.springframework.util.StreamUtils.copyToString(es, java.nio.charset.StandardCharsets.UTF_8);
                log.warn("Fast2SMS API returned error [{}]: {}", responseCode, errResp);
                return "Fast2SMS error [" + responseCode + "]: " + errResp;
            }
        } catch (Exception ex) {
            log.error("Fast2SMS dispatch error", ex);
            return "Fast2SMS dispatch failed: " + ex.getMessage();
        }
    }

    /**
     * Cleans credentials by trimming whitespace and removing wrapping quotes.
     */
    private String cleanCredential(String val) {
        if (!StringUtils.hasText(val)) {
            return null;
        }
        String cleaned = val.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
            (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned.isEmpty() ? null : cleaned;
    }

    /**
     * Resolves Account SID with priority: Environment / Properties -> Parameter Entity.
     */
    private String resolveAccountSid(BLRS_EmailSmsParam_Entity p) {
        if (StringUtils.hasText(twilioAccountSid)) {
            return cleanCredential(twilioAccountSid);
        }
        if (p != null && StringUtils.hasText(p.getSms_user())) {
            return cleanCredential(p.getSms_user());
        }
        return null;
    }

    /**
     * Resolves Auth Token with priority: Environment / Properties -> Parameter Entity.
     */
    private String resolveAuthToken(BLRS_EmailSmsParam_Entity p) {
        if (StringUtils.hasText(twilioAuthToken)) {
            return cleanCredential(twilioAuthToken);
        }
        if (p != null && StringUtils.hasText(p.getSms_pwd())) {
            String decryptedToken = com.bornfire.config.AES.decryptSafe(p.getSms_pwd());
            return cleanCredential(decryptedToken);
        }
        return null;
    }

    /**
     * Resolves Sender Phone with priority: Environment / Properties -> Parameter Entity.
     */
    private String resolveFromPhone(BLRS_EmailSmsParam_Entity p) {
        if (StringUtils.hasText(twilioPhoneNumber)) {
            return cleanCredential(twilioPhoneNumber);
        }
        if (p != null && StringUtils.hasText(p.getSms_host())) {
            String host = cleanCredential(p.getSms_host());
            if (host != null && host.matches("^[+]?[0-9]+$")) {
                return host;
            }
        }
        return null;
    }

    /**
     * Creates or returns existing TwilioRestClient.
     */
    protected TwilioRestClient getOrCreateClient(String sid, String token) {
        if (this.customRestClient != null) {
            return this.customRestClient;
        }
        return new TwilioRestClient.Builder(sid, token).build();
    }

    /**
     * Normalizes a phone number to standard E.164 format (+[country code][subscriber number]).
     */
    public String formatToE164(String rawPhone) {
        if (!StringUtils.hasText(rawPhone)) {
            return null;
        }

        String cleaned = rawPhone.trim().replaceAll("[^0-9+]", "");
        if (cleaned.isEmpty()) {
            return null;
        }

        // Already starts with +
        if (cleaned.startsWith("+")) {
            String digits = cleaned.substring(1);
            if (digits.length() >= 7 && digits.length() <= 15 && digits.matches("\\d+")) {
                return cleaned;
            }
            return null;
        }

        // Strip leading zeros (e.g. 09342070589 -> 9342070589)
        if (cleaned.startsWith("0")) {
            cleaned = cleaned.replaceFirst("^0+", "");
        }

        String prefix = StringUtils.hasText(defaultCountryCode) ? defaultCountryCode.trim() : "+91";
        if (!prefix.startsWith("+")) {
            prefix = "+" + prefix;
        }

        // Common 10-digit mobile (e.g. in India)
        if (cleaned.length() == 10 && cleaned.matches("\\d{10}")) {
            return prefix + cleaned;
        }

        // 12-digit mobile starting with 91 (e.g. 919342070589)
        if (cleaned.length() == 12 && cleaned.startsWith("91")) {
            return "+" + cleaned;
        }

        // 11-digit mobile starting with 230 (e.g. Mauritius 23057123456)
        if (cleaned.length() == 11 && cleaned.startsWith("230")) {
            return "+" + cleaned;
        }

        // 11-digit mobile starting with 1 (e.g. US 1XXXXXXXXXX)
        if (cleaned.length() == 11 && cleaned.startsWith("1")) {
            return "+" + cleaned;
        }

        // Generic length between 8 and 15
        if (cleaned.length() >= 8 && cleaned.length() <= 15 && cleaned.matches("\\d+")) {
            return prefix + cleaned;
        }

        return null;
    }

    /**
     * Masks sensitive phone numbers for safe logging (e.g., +919342070589 -> +91****0589).
     */
    public String mask(String phone) {
        if (phone == null || phone.length() < 5) {
            return "***";
        }
        int len = phone.length();
        int prefixLen = Math.min(3, len - 2);
        int suffixLen = Math.min(4, len - prefixLen);
        return phone.substring(0, prefixLen) + "****" + phone.substring(len - suffixLen);
    }
}