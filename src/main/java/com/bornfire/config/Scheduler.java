package com.bornfire.config;

import java.sql.SQLException;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class Scheduler {
	
	//@Scheduled(fixedRate = 5000)
	/*
	 * public String sendmails( ) throws SQLException, ParseException {
	 * System.out.println("Hi"); String to = "ragul.r@bornfire.in"; String from
	 * ="accts@bornfire.in"; String username = "accts@bornfire.in";//change
	 * accordingly String password = "VNivas@636003";//change accordingly String
	 * host = "sg2plzcpnl491716.prod.sin2.secureserver.net"; return
	 * sendingmails.sendmail( from, username, password, to,host ) ;
	 * 
	 * }
	 */
		/*@Scheduled(fixedRate = 10000)
				public String spsendmails( ) throws SQLException, ParseException {
					System.out.println("Hi");
					String to = "siddhaiyan@bornfire.in";
					   String from = "accts@bornfire.in";
					   String username = "accts@bornfire.in";//change accordingly
					   String password = "VNivas@636003";//change accordingly
					   String host = "sg2plzcpnl491716.prod.sin2.secureserver.net";
					   String Sp="STACKPOS";
					   String inv_due_date="01-JUL-2023";
					   String inv_date="31-JUL-2023";
					return spsendingmail.sendmail(  from,  username,  password, to,host ,Sp, inv_due_date, inv_date ) ;
					
					
				}*/
}
