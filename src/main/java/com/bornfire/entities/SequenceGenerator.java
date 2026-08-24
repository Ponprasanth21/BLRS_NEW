package com.bornfire.entities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

public class SequenceGenerator {
	
	@Autowired
	BGLSAuditTable_Rep bGLSAuditTable_Rep;
	//private static final String CHAR_LIST = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int DOC_SEQ_ID = 5;
	private static final String NUM_LIST= "0123456789";
	private static final int MERCHANT_FEE = 7;
	private static final int MERCHANT_QR_P_ID = 6;


	private int getRandomNumber() {
		int randomInt = 0;
		Random randomGenerator = new Random();
		randomInt = randomGenerator.nextInt(NUM_LIST.length());

		if (randomInt - 1 == -1) {
			return randomInt;
		} else {
			return randomInt - 1;
		}
	}
	
	public String generateDocRefID() {

		StringBuffer randStr = new StringBuffer();
		randStr.append("CIM");
		randStr.append(new SimpleDateFormat("YYMMdd").format(new Date()));
		
		for (int i = 0; i < DOC_SEQ_ID; i++) {
			int number = getRandomNumber();
			char ch = NUM_LIST.charAt(number);
			randStr.append(ch);
		}
		return randStr.toString();
	}
	
	public String generateMerchantQRPID() {

		StringBuffer randStr = new StringBuffer();
		randStr.append("CIM");
		randStr.append(new SimpleDateFormat("YYMMdd").format(new Date()));
		
		for (int i = 0; i < MERCHANT_QR_P_ID; i++) {
			int number = getRandomNumber();
			char ch = NUM_LIST.charAt(number);
			randStr.append(ch);
		}
		return randStr.toString();
	}
	
	public String generateMandateRefNo() {

		StringBuffer randStr = new StringBuffer();
		randStr.append("MAN");
		randStr.append(new SimpleDateFormat("YYMMdd").format(new Date()));
		
		for (int i = 0; i < DOC_SEQ_ID; i++) {
			int number = getRandomNumber();
			char ch = NUM_LIST.charAt(number);
			randStr.append(ch);
		}
		return randStr.toString();
	}
	
	public String generateMerchantRefNo() {

		StringBuffer randStr = new StringBuffer();
		randStr.append("MER");
		randStr.append(new SimpleDateFormat("YYMMdd").format(new Date()));
		
		for (int i = 0; i < DOC_SEQ_ID; i++) {
			int number = getRandomNumber();
			char ch = NUM_LIST.charAt(number);
			randStr.append(ch);
		}
		return randStr.toString();
	}
	
	
	
	public String generateMerchantFeeRefNo() {

		StringBuffer randStr = new StringBuffer();
		
		//randStr.append(new SimpleDateFormat("YYMMdd").format(new Date()));
		
		for (int i = 0; i < MERCHANT_FEE; i++) {
			int number = getRandomNumber();
			char ch = NUM_LIST.charAt(number);
			randStr.append(ch);
		}
		return randStr.toString();
	}
	
	
	public String generateRequestUUId() {

		StringBuffer randStr = new StringBuffer();
		randStr.append(new SimpleDateFormat("yyyyMMdd").format(new Date()));
		randStr.append("_");
		
		Long request_UUID=bGLSAuditTable_Rep.getAuditRefUUID();
		
		randStr.append(String.format("%05d", request_UUID));
		
		return randStr.toString();
	}
}
