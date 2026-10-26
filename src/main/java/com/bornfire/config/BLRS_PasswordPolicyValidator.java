package com.bornfire.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Enterprise Password Policy Validator and BCrypt Encoder for BLRS.
 *
 * Ground Rules & Security Specifications:
 * - Passwords: BCrypt only (strength 10).
 * - Minimum 8 characters in length.
 * - At least one uppercase letter (A-Z).
 * - At least one lowercase letter (a-z).
 * - At least one digit (0-9).
 * - At least one special character (!@#$%^&*()_+-=[]{};':"|,.<>/?).
 * - Backward compatibility: Transparently verifies legacy AES passwords and
 *   signals needsRehash() so the caller can auto-upgrade the hash in the database.
 */
@Component
public class BLRS_PasswordPolicyValidator {

    private static final Logger log = LoggerFactory.getLogger(BLRS_PasswordPolicyValidator.class);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    /**
     * Validates raw password against the strict BLRS password policy.
     *
     * @param rawPassword user entered password
     * @return null if valid, or a descriptive error message if invalid
     */
    public String validatePolicy(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            return "Password cannot be empty.";
        }
        if (rawPassword.length() < MIN_LENGTH) {
            return "Password must be at least " + MIN_LENGTH + " characters long.";
        }
        if (!UPPERCASE_PATTERN.matcher(rawPassword).find()) {
            return "Password must contain at least one uppercase letter (A-Z).";
        }
        if (!LOWERCASE_PATTERN.matcher(rawPassword).find()) {
            return "Password must contain at least one lowercase letter (a-z).";
        }
        if (!DIGIT_PATTERN.matcher(rawPassword).find()) {
            return "Password must contain at least one numeric digit (0-9).";
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(rawPassword).find()) {
            return "Password must contain at least one special character (!@#$%^&* etc.).";
        }
        return null; // Valid
    }

    /**
     * Encodes raw password with BCrypt.
     */
    public String encode(String rawPassword) {
        if (rawPassword == null) return null;
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifies raw password against the stored database hash.
     * Supports BCrypt hashes as well as legacy AES passwords for transparent migration.
     */
    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }

        // 1. Standard BCrypt hash check (starts with $2a$, $2b$, or $2y$)
        if (isBCrypt(storedHash)) {
            try {
                return passwordEncoder.matches(rawPassword, storedHash);
            } catch (Exception e) {
                log.warn("BCrypt matching failed: {}", e.getMessage());
                return false;
            }
        }

        // 2. Legacy AES encrypted password check (transparent backward compatibility)
        try {
            boolean legacyValid = AES.validatePassword(rawPassword, storedHash);
            if (legacyValid) {
                log.info("Legacy AES password matched successfully. Will be auto-upgraded to BCrypt.");
                return true;
            }
        } catch (Exception e) {
            log.debug("Legacy AES validation check error: {}", e.getMessage());
        }

        // 3. Fallback check for plain text (in case of default seed data)
        if (rawPassword.equals(storedHash)) {
            log.info("Plaintext password matched. Will be auto-upgraded to BCrypt.");
            return true;
        }

        return false;
    }

    /**
     * Returns true if the stored hash is NOT yet a BCrypt hash, indicating
     * that it should be transparently re-hashed and updated in the DB upon successful login.
     */
    public boolean needsRehash(String storedHash) {
        return storedHash == null || !isBCrypt(storedHash);
    }

    private boolean isBCrypt(String hash) {
        return hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
