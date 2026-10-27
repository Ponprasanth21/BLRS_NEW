package com.bornfire.config;

import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Component

public class AES {
	private static String algorithm = "AES";
	private static byte[] keyValue = new byte[] { 'A', 'S', 'e', 'c', 'u', 'r', 'e', 'S', 'e', 'c', 'r', 'e', 't', 'K',
			'e', 'y' };

	// Performs Encryption
	public static String encrypt(String plainText) throws Exception {
		Key key = generateKey();
		Cipher chiper = Cipher.getInstance(algorithm);
		chiper.init(Cipher.ENCRYPT_MODE, key);

		byte[] encrypted = chiper.doFinal(plainText.getBytes());

		String encryptedValue = Base64.getEncoder().encodeToString(encrypted);

		return encryptedValue;
	}

	// Performs decryption
	public static String decrypt(String encryptedText) throws Exception {
		// generate key
		Key key = generateKey();
		Cipher chiper = Cipher.getInstance(algorithm);
		chiper.init(Cipher.DECRYPT_MODE, key);

		return new String(chiper.doFinal(Base64.getDecoder().decode(encryptedText)));

	}

//generateKey() is used to generate a secret key for AES algorithm
	private static Key generateKey() throws Exception {
		Key key = new SecretKeySpec(keyValue, algorithm);
		return key;
	}

	public String decpwd(String Name) throws Exception {

		String decryptedText = AES.decrypt(Name);
		return decryptedText;
	}

	public String encpwd(String Name) throws Exception {

		String encryptedText = AES.encrypt(Name);
		return encryptedText;
	}

	public static boolean validatePassword(String originalPassword, String storedPassword)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		try {

			if (originalPassword.equals(AES.decrypt(storedPassword))) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			return false;
		}

	}

	public static void main(String arg[]) throws Exception {
		System.out.println(encrypt("Bornfire@123"));
		System.out.println("here");

	}

}
