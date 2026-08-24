package com.bornfire.config;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.bornfire.entities.BLRS_UserProfile_Repo;

@Configuration
@Component
@Service

public class Emailsent {
	@Autowired
	BLRS_UserProfile_Repo userProfileRep;

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	Environment env;

	@Autowired
	DataSource srcdataSource;

	private static final Logger logger = LoggerFactory.getLogger(Emailsent.class);

	public String execute(String emailid) throws Exception {

		String nextPage = "";
		String status;

		// String host = "webmail.bornfire.in";
		String host = "sg2plcpnl0144.prod.sin2.secureserver.net";
		String port = "2096";
		// String port = "25";
		final String user = "tamilarasi.p@bornfire.in";// change accordingly
		final String password = "Feb01-02-1998";// change accordingly
		// String host = env.getProperty("mail.host");
		// String user = env.getProperty("mail.username");// change accordingly
		// String password = env.getProperty("mail.password");// change accordingly

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		// props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		});
		System.out.println("SEND MAIL...");

		try {
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(user));

			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailid)); // have to add the user mail
			// msg.addRecipient(Message.RecipientType.CC, new
			// InternetAddress(cmnVal.getEmail_id_cc1()));

			msg.setSentDate(new Date());
			msg.setSubject("testing");
			msg.setText("testing mail body");

			/*
			 * StringBuilder sb = new StringBuilder();
			 * //sb.append("Dear Sir,").append(System.lineSeparator());
			 * sb.append("Please find below the alert particulars,").append(System.
			 * lineSeparator());
			 * 
			 * sb.append(cmnVal.getEmail_body());
			 * 
			 * msg.setText(sb.toString());
			 */

			// String det = cmnVal.getEmail_body();

			// String[] dt1 = det.split("/");
			String message1 = "<i>We received your issue our enginners are working on it.,</i><br>";
//						for (String info : dt1) {
			//
//							message1 += "<b>" + info + "</b><br>";
			//
//						}
			// System.out.println(message1);
			msg.setContent(message1, "text/html");
			Transport.send(msg);

			nextPage = "success";

		} catch (Exception E) {
			System.out.println("Oops something has gone pearshaped!");
			System.out.println(E);
			nextPage = "error";
		}

		return nextPage;

	}
}
