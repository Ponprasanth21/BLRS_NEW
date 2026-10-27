package com.bornfire.services;

import com.bornfire.entities.BLRS_EmailSmsParam_Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Sends SMS through the HTTP gateway configured in
 * Admin > Parameters > Email and SMS (SMS section).
 *
 * Supports standard HTTP GET/POST SMS gateways with query parameters or URL template placeholders:
 * {user}, {pass}, {to}, {msg}.
 */
@Service
public class BLRS_SmsService {

    private static final Logger log = LoggerFactory.getLogger(BLRS_SmsService.class);

    @Value("${notification.sms.timeout:8000}")
    private int timeoutMs;

    /**
     * @return {@code null} on success, otherwise error message.
     */
    public String send(BLRS_EmailSmsParam_Entity p, String mobile, String text) {

        if (!StringUtils.hasText(mobile)) return "Recipient mobile is empty.";
        String cleanMobile = mobile.trim().replaceAll("[^0-9+]", "");
        if (cleanMobile.isEmpty()) {
            return "Invalid recipient mobile number: " + mobile;
        }

        if (!"Y".equalsIgnoreCase(p.getSms_flg())) return "SMS is disabled in Parameters.";
        if (!StringUtils.hasText(p.getSms_host())) {
            return "SMS gateway host not configured.";
        }

        String rawHost = p.getSms_host().trim();
        if (!rawHost.startsWith("http://") && !rawHost.startsWith("https://")) {
            rawHost = "https://" + rawHost;
        }

        String user = p.getSms_user() != null ? p.getSms_user().trim() : "";
        String pwd  = p.getSms_pwd()  != null ? p.getSms_pwd().trim()  : "";
        String msgText = text != null ? text : "";

        HttpURLConnection conn = null;
        try {
            String url;
            // Support URL template with placeholders if defined by administrator
            if (rawHost.contains("{") && rawHost.contains("}")) {
                url = rawHost
                        .replace("{user}",     URLEncoder.encode(user, "UTF-8"))
                        .replace("{username}", URLEncoder.encode(user, "UTF-8"))
                        .replace("{pass}",     URLEncoder.encode(pwd,  "UTF-8"))
                        .replace("{password}", URLEncoder.encode(pwd,  "UTF-8"))
                        .replace("{pwd}",      URLEncoder.encode(pwd,  "UTF-8"))
                        .replace("{to}",       URLEncoder.encode(cleanMobile, "UTF-8"))
                        .replace("{mobile}",   URLEncoder.encode(cleanMobile, "UTF-8"))
                        .replace("{phone}",    URLEncoder.encode(cleanMobile, "UTF-8"))
                        .replace("{msg}",      URLEncoder.encode(msgText, "UTF-8"))
                        .replace("{message}",  URLEncoder.encode(msgText, "UTF-8"))
                        .replace("{text}",     URLEncoder.encode(msgText, "UTF-8"));
            } else {
                // Classic standard SMS-over-HTTP-GET pattern
                StringBuilder sb = new StringBuilder(rawHost);
                sb.append(rawHost.contains("?") ? "&" : "?");
                if (StringUtils.hasText(user)) {
                    sb.append("user=").append(URLEncoder.encode(user, "UTF-8")).append("&");
                }
                if (StringUtils.hasText(pwd)) {
                    sb.append("pass=").append(URLEncoder.encode(pwd, "UTF-8")).append("&");
                }
                sb.append("to=").append(URLEncoder.encode(cleanMobile, "UTF-8"))
                  .append("&msg=").append(URLEncoder.encode(msgText, "UTF-8"));
                url = sb.toString();
            }

            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            int effectiveTimeout = timeoutMs > 0 ? timeoutMs : 8000;
            conn.setConnectTimeout(effectiveTimeout);
            conn.setReadTimeout(effectiveTimeout);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (BLRS-NotificationEngine)");

            int code = conn.getResponseCode();
            String body = readBody(conn, code);

            if (code >= 200 && code < 300) {
                log.info("SMS sent to {} -> {}", mask(cleanMobile), body);
                return null;
            }
            log.warn("SMS gateway HTTP {} for {}: {}", code, mask(cleanMobile), body);
            return "Gateway HTTP " + code + (StringUtils.hasText(body) ? ": " + body : "");

        } catch (SocketTimeoutException te) {
            log.error("SMS to {} timed out after {}ms", mask(cleanMobile), timeoutMs);
            return "SMS Gateway timed out after " + timeoutMs + "ms";
        } catch (Exception e) {
            log.error("SMS to {} failed: {}", mask(cleanMobile), e.getMessage(), e);
            return "SMS failed: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Tests SMS gateway connectivity and configuration.
     */
    public String testConnection(BLRS_EmailSmsParam_Entity p, String testMobile) {
        if (!StringUtils.hasText(testMobile)) {
            return "Test mobile number is required.";
        }
        String testText = "BLRS Test Notification: SMS gateway connection verified successfully.";
        return send(p, testMobile, testText);
    }

    private String readBody(HttpURLConnection conn, int code) {
        try {
            InputStream is = (code >= 200 && code < 400)
                    ? conn.getInputStream()
                    : conn.getErrorStream();
            if (is == null) return "";
            try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(line);
                }
                return sb.toString().trim();
            }
        } catch (Exception e) {
            return "";
        }
    }

    private String mask(String m) {
        if (m == null || m.length() < 4) return "***";
        return m.substring(0, 2) + "****" + m.substring(m.length() - 2);
    }
}