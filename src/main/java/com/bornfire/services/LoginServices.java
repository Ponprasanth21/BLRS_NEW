package com.bornfire.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import com.bornfire.config.AES;
import com.bornfire.config.Emailsent;
import com.bornfire.config.PasswordEncryption;
import com.bornfire.entities.BLRS_Access_Role_Entity;
import com.bornfire.entities.BLRS_Access_Role_Repo;
import com.bornfire.entities.BLRS_BusinessTable_Entity;
import com.bornfire.entities.BLRS_BusinessTable_Rep;
import com.bornfire.entities.BLRS_UserProfile_Entity;
import com.bornfire.entities.BLRS_UserProfile_Repo;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@Service
@ConfigurationProperties("output")
@Transactional
public class LoginServices {

	private static final Logger logger = LoggerFactory.getLogger(LoginServices.class);

	@Autowired
	BLRS_UserProfile_Repo userProfileRep;

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	DataSource srcdataSource;

	@Autowired
	BLRS_Access_Role_Repo access_Role_Repo;

	@Autowired
	BLRS_BusinessTable_Rep bGLSBusinessTable_Rep;

	@NotNull
	private String exportpath;

	// @Value("${default.password}")
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getExportpath() {
		return exportpath;
	}

	public void setExportpath(String exportpath) {
		this.exportpath = exportpath;
	}

	/*
	 * Getting 3 inputs -
	 * 
	 * UserProfile Object, Formmode - Valid values : add, edit, inputuser - user who
	 * edited the data
	 * 
	 * if formmode is add - Get password from application.properties create the user
	 * 
	 * if formmode is edit - Get password from database for that user and use other
	 * fields came from front end.
	 * 
	 * 
	 */

	public String addUser(BLRS_UserProfile_Entity userProfile, String formmode, String inputUser)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		String msg = "";

		if (formmode.equals("add")) {

			BLRS_UserProfile_Entity up = new BLRS_UserProfile_Entity();

			String encryptedPassword = PasswordEncryption.getEncryptedPassword(up.getPassword());

			if (up.getLogin_status().equals("Active")) {
				up.setUser_locked_flg("N");
			} else {
				up.setUser_locked_flg("Y");
			}

			if (up.getUser_status().equals("Active")) {
				up.setDisable_flg("N");
			} else {
				up.setDisable_flg("Y");
			}

			up.setEntity_flg("N");
			up.setEntry_time(new Date());
			up.setEntry_user(inputUser);
			up.setModify_user(inputUser);
			up.setModify_time(new Date());
			up.setDel_flg("N");
			up.setLogin_flg("N");
			up.setNo_of_attmp(0);
			up.setPassword(encryptedPassword);

			/*
			 * Emailsent emailfun = new Emailsent(); try { String message =
			 * emailfun.execute( up.getEmail_id()); } catch (Exception e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); }
			 */

			userProfileRep.save(up);

			// FOR AUIDT
			BLRS_BusinessTable_Entity audit = new BLRS_BusinessTable_Entity();
			Long auditID = bGLSBusinessTable_Rep.getBusinessRefUUID();
			Optional<BLRS_UserProfile_Entity> up1 = userProfileRep.findById(inputUser);
			BLRS_UserProfile_Entity user = up1.get();
			LocalDateTime currentDateTime = LocalDateTime.now();
			Date dateValue = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
			audit.setAudit_date(new Date());
			audit.setEntry_time(dateValue);
			audit.setEntry_user(user.getUserid());
			audit.setFunc_code("User Id");
			audit.setRemarks("User Created Successfully");
			audit.setAudit_table("BGLS_USER_PROFILE_TABLE");
			audit.setAudit_screen("USER PROFILE - ADD");
			audit.setEvent_id(user.getUserid());
			audit.setEvent_name(user.getUsername());
			// audit.setModi_details("Login Successfully");
			BLRS_UserProfile_Entity auth_user = userProfileRep.getRole(user.getUserid());
			String auth_user_val = auth_user.getAuth_user();
			Date auth_user_date = auth_user.getAuth_time();
			audit.setAuth_user(auth_user_val);
			audit.setAuth_time(auth_user_date);
			audit.setAudit_ref_no(auditID.toString());
			audit.setField_name("-");

			bGLSBusinessTable_Rep.save(audit);

			msg = "User Created Successfully";

		}

		else if (formmode.equals("modify")) {

			BLRS_UserProfile_Entity original = userProfileRep.getRole(userProfile.getUserid());

			BLRS_UserProfile_Entity up = new BLRS_UserProfile_Entity();

			String encryptedPassword = PasswordEncryption.getEncryptedPassword(up.getPassword());

			if (up.getLogin_status().equals("Active")) {
				up.setUser_locked_flg("N");
			} else {
				up.setUser_locked_flg("Y");
			}

			if (up.getUser_status().equals("Active")) {
				up.setDisable_flg("N");
			} else {
				up.setDisable_flg("Y");
			}

			up.setModify_time(new Date());
			up.setModify_user(inputUser);
			up.setModify_flg("Y");
			up.setDel_flg("N");
			up.setLogin_flg("N");
			up.setNo_of_attmp(0);
			up.setEntry_user(original.getEntry_user());
			up.setEntry_time(original.getEntry_time());
			up.setAuth_user(original.getAuth_user());
			up.setAuth_time(original.getAuth_time());
			up.setEntity_flg("N");
			up.setPassword(encryptedPassword);

			userProfileRep.save(up);
			msg = "User Modified Successfully";

		}

		return msg;

	}

	/* Praveen */
	public String verifyUser(String userId, String inputUser) {
		String msg = "";

		BLRS_UserProfile_Entity userProfile = userProfileRep.getRole(userId);
		BLRS_Access_Role_Entity accessRole = access_Role_Repo.getRole(userId);
		if (Objects.nonNull(userProfile.getUserid())) {
			userProfile.setEntity_flg("Y");
			userProfile.setAuth_user(inputUser);
			userProfile.setAuth_time(new Date());
			userProfileRep.save(userProfile);

			accessRole.setEntity_flg("Y");
			accessRole.setAuth_user(inputUser);
			accessRole.setAuth_time(new Date());
			access_Role_Repo.save(accessRole);

			msg = "User Verified Successfully";

			// FOR AUIDT
			BLRS_BusinessTable_Entity audit = new BLRS_BusinessTable_Entity();
			Long auditID = bGLSBusinessTable_Rep.getBusinessRefUUID();
			Optional<BLRS_UserProfile_Entity> up1 = userProfileRep.findById(inputUser);
			BLRS_UserProfile_Entity user = up1.get();
			LocalDateTime currentDateTime = LocalDateTime.now();
			Date dateValue = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
			audit.setAudit_date(new Date());
			audit.setEntry_time(dateValue);
			audit.setEntry_user(user.getUserid());
			audit.setFunc_code("User Id");
			audit.setRemarks("User Verified Successfully");
			audit.setAudit_table("BGLS_USER_PROFILE_TABLE");
			audit.setAudit_screen("USER PROFILE - VERIFY");
			audit.setEvent_id(user.getUserid());
			audit.setEvent_name(user.getUsername());
			// audit.setModi_details("Login Successfully");
			BLRS_UserProfile_Entity auth_user = userProfileRep.getRole(user.getUserid());
			String auth_user_val = auth_user.getAuth_user();
			Date auth_user_date = auth_user.getAuth_time();
			audit.setAuth_user(auth_user_val);
			audit.setAuth_time(auth_user_date);
			audit.setAudit_ref_no(auditID.toString());
			audit.setField_name("-");

			bGLSBusinessTable_Rep.save(audit);

		} else {
			msg = "User Not Found";
		}

		return msg;
	}

	/* Praveen */
	public String deleteUser(String userId, String deleteType, String userid1) {
		String msg = "";

		BLRS_UserProfile_Entity userProfile = userProfileRep.getRole(userId);

		if (deleteType.equals("Y")) {
			/* Verified record delete */
			userProfile.setDel_flg("Y");
			userProfileRep.save(userProfile);
			msg = "User Deleted Successfully";

			// FOR AUIDT
			BLRS_BusinessTable_Entity audit = new BLRS_BusinessTable_Entity();
			Long auditID = bGLSBusinessTable_Rep.getBusinessRefUUID();
			Optional<BLRS_UserProfile_Entity> up1 = userProfileRep.findById(userid1);
			BLRS_UserProfile_Entity user = up1.get();
			LocalDateTime currentDateTime = LocalDateTime.now();
			Date dateValue = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
			audit.setAudit_date(new Date());
			audit.setEntry_time(dateValue);
			audit.setEntry_user(user.getUserid());
			audit.setFunc_code("User Id");
			audit.setRemarks("User Deleted Successfully");
			audit.setAudit_table("BGLS_USER_PROFILE_TABLE");
			audit.setAudit_screen("USER PROFILE - DELETE");
			audit.setEvent_id(user.getUserid());
			audit.setEvent_name(user.getUsername());
			// audit.setModi_details("Login Successfully");
			BLRS_UserProfile_Entity auth_user = userProfileRep.getRole(user.getUserid());
			String auth_user_val = auth_user.getAuth_user();
			Date auth_user_date = auth_user.getAuth_time();
			audit.setAuth_user(auth_user_val);
			audit.setAuth_time(auth_user_date);
			audit.setAudit_ref_no(auditID.toString());
			audit.setField_name("-");

			bGLSBusinessTable_Rep.save(audit);

		} else {

			/* UnVerified record delete */
			userProfileRep.deleteById(userId);
			msg = "User Deleted Successfully";
			// FOR AUIDT
			BLRS_BusinessTable_Entity audit = new BLRS_BusinessTable_Entity();
			Long auditID = bGLSBusinessTable_Rep.getBusinessRefUUID();
			Optional<BLRS_UserProfile_Entity> up1 = userProfileRep.findById(userid1);
			BLRS_UserProfile_Entity user = up1.get();
			LocalDateTime currentDateTime = LocalDateTime.now();
			Date dateValue = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
			audit.setAudit_date(new Date());
			audit.setEntry_time(dateValue);
			audit.setEntry_user(user.getUserid());
			audit.setFunc_code("User Id");
			audit.setRemarks("User Deleted Successfully");
			audit.setAudit_table("BGLS_USER_PROFILE_TABLE");
			audit.setAudit_screen("USER PROFILE - DELETE");
			audit.setEvent_id(user.getUserid());
			audit.setEvent_name(user.getUsername());
			// audit.setModi_details("Login Successfully");
			BLRS_UserProfile_Entity auth_user = userProfileRep.getRole(user.getUserid());
			String auth_user_val = auth_user.getAuth_user();
			Date auth_user_date = auth_user.getAuth_time();
			audit.setAuth_user(auth_user_val);
			audit.setAuth_time(auth_user_date);
			audit.setAudit_ref_no(auditID.toString());
			audit.setField_name("-");

			bGLSBusinessTable_Rep.save(audit);

		}

		return msg;
	}

	public Iterable<BLRS_UserProfile_Entity> getUsersList() {

		Iterable<BLRS_UserProfile_Entity> users = userProfileRep.findAll();

		return users;

	}

	public BLRS_UserProfile_Entity getUser(String id) {
		logger.info(id);
		if (userProfileRep.existsById(id)) {
			BLRS_UserProfile_Entity up = userProfileRep.findById(id).get();
			logger.info(up.getEntity_flg());
			return up;
		} else {
			return new BLRS_UserProfile_Entity();
		}

	};

	public String verifyUser(BLRS_UserProfile_Entity userProfile, String inputUser) {
		String msg = "";

		Optional<BLRS_UserProfile_Entity> up = userProfileRep.findById(userProfile.getUserid());

		try {

			if (up.isPresent()) {

				userProfile.setPassword(up.get().getPassword());

				if (userProfile.getLogin_status().equals("Active")) {
					userProfile.setUser_locked_flg("N");
				} else {
					userProfile.setUser_locked_flg("Y");
				}

				if (userProfile.getUser_status().equals("Active")) {
					userProfile.setDisable_flg("N");
				} else {
					userProfile.setDisable_flg("Y");
				}

				userProfile.setNo_of_attmp(0);
				userProfile.setEntity_flg("Y");
				userProfile.setLogin_flg("N");
				userProfile.setAuth_user(inputUser);
				userProfile.setAuth_time(new Date());

				userProfileRep.save(userProfile);
				// FOR AUIDT
				BLRS_BusinessTable_Entity audit = new BLRS_BusinessTable_Entity();
				Long auditID = bGLSBusinessTable_Rep.getBusinessRefUUID();
				Optional<BLRS_UserProfile_Entity> up1 = userProfileRep.findById(inputUser);
				BLRS_UserProfile_Entity user = up1.get();
				LocalDateTime currentDateTime = LocalDateTime.now();
				Date dateValue = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
				audit.setAudit_date(new Date());
				audit.setEntry_time(dateValue);
				audit.setEntry_user(user.getUserid());
				audit.setFunc_code("User Id");
				audit.setRemarks("User Verified Successfully");
				audit.setAudit_table("BGLS_USER_PROFILE_TABLE");
				audit.setAudit_screen("USER PROFILE - VERIFY");
				audit.setEvent_id(user.getUserid());
				audit.setEvent_name(user.getUsername());
				// audit.setModi_details("Login Successfully");
				BLRS_UserProfile_Entity auth_user = userProfileRep.getRole(user.getUserid());
				String auth_user_val = auth_user.getAuth_user();
				Date auth_user_date = auth_user.getAuth_time();
				audit.setAuth_user(auth_user_val);
				audit.setAuth_time(auth_user_date);
				audit.setAudit_ref_no(auditID.toString());
				audit.setField_name("-");

				bGLSBusinessTable_Rep.save(audit);
			}

			msg = "User Verified Successfully";

		} catch (Exception e) {
			logger.info(e.getMessage());
			e.printStackTrace();
			msg = "Error Occured. Please contact Administrator";
		}

		return msg;
	}

	public String passwordReset(BLRS_UserProfile_Entity userprofile, String userid) {

		String msg = "";

		try {
			String encryptedPassword = PasswordEncryption.getEncryptedPassword(this.password);

			Optional<BLRS_UserProfile_Entity> up = userProfileRep.findById(userprofile.getUserid());

			if (up.isPresent()) {

				BLRS_UserProfile_Entity user = up.get();

				user.setPassword(encryptedPassword);

				user.setNo_of_attmp(0);
				user.setLogin_flg("N");
				user.setUser_locked_flg("N");
				userProfileRep.save(user);
			}

			msg = "Password Resetted Successfully";

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {

			e.printStackTrace();

			msg = "Error Occured. Please contact Administrator";
		}

		return msg;
	}

	/*
	 * Getting LoginFlg -
	 * 
	 * If loginFlg = 'N' - User should be prompted to change password. else thats
	 * not required.
	 * 
	 * Loginflg ='N' will be updated at the time of new user creation and at the
	 * time of password reset by admin.
	 * 
	 */

	public String checkPasswordChangeReq(String userid) {

		Optional<BLRS_UserProfile_Entity> up = userProfileRep.findById(userid);
		String loginflg = up.get().getLogin_flg();

		return loginflg;
	}

	/*
	 * public String changePassword(String oldpass, String newpass, String userid) {
	 * 
	 * String msg = ""; Optional<ResourceMaster> up =
	 * resourceMasterRepo.findById(userid); String encrypted_password =null; try {
	 * encrypted_password = AES.encrypt(oldpass); } catch (Exception e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } ResourceMaster user =
	 * up.get();
	 * 
	 * if(encrypted_password.equals(user.getPassword())) { if (up.isPresent()) {
	 * 
	 * 
	 * // if (PasswordEncryption.validatePassword(encrypted_password,
	 * user.getPassword())) {
	 * 
	 * // if (!PasswordEncryption.validatePassword(newpass, user.getPassword())) {
	 * 
	 * String encryptedPassword = null; try { encryptedPassword =
	 * AES.encrypt(newpass); } catch (Exception e) { // TODO Auto-generated catch
	 * block e.printStackTrace(); } System.out.println(encryptedPassword+"new one");
	 * 
	 * user.setPassword(encryptedPassword); // user.setDel_flg("Y");
	 * 
	 * final Calendar cal = Calendar.getInstance(); cal.add(Calendar.MONTH,1);
	 * System.out.println(cal.getTime()); user.setPw_expy_date(cal.getTime());
	 * 
	 * resourceMasterRepo.save(user);
	 * 
	 * msg = "Password Changed Successfully";
	 * 
	 * } else {
	 * 
	 * msg = "New password cannot be Same as Old password"; }
	 * 
	 * } else { msg = "Incorrect Old Password!"; }
	 * 
	 * 
	 * return msg; };
	 */

	public void SessionLogging(String menuname, String menuid, String sessionid, String userid, String ip,
			String status) {
		Session hs = sessionFactory.getCurrentSession();

		try {

			if (menuname.equals("LOGOUT")) {

				hs.createQuery("update XBRLSession set status='IN-ACTIVE' where session_id = ?1")
						.setParameter(1, sessionid).executeUpdate();

			} /*
				 * else {
				 * 
				 * hs.save(new BTMSession(menuname, menuid, sessionid, userid, ip, new Date(),
				 * status)); }
				 */

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public File getUserLogFile(Date fromdate, Date todate) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

		String path = exportpath;
		String fileName = "USER_LOGS_" + dateFormat.format(new Date()) + ".xlsx";
		File outputFile;

		File jasperFile;

		File folders = new File(path);
		if (!folders.exists()) {
			folders.mkdirs();
		}

		try {
			jasperFile = ResourceUtils.getFile("classpath:static/jasper/USER_LOGS/UserLogs.jasper");
			JasperReport jr = (JasperReport) JRLoader.loadObject(jasperFile);
			HashMap<String, Object> map = new HashMap<String, Object>();

			logger.info("Assigning Parameters for Jasper");
			map.put("FromDate", dateFormat.format(fromdate));
			map.put("ToDate", dateFormat.format(todate));

			path = path + "/" + fileName;
			JasperPrint jp = JasperFillManager.fillReport(jr, map, srcdataSource.getConnection());
			JRXlsxExporter exporter = new JRXlsxExporter();
			exporter.setExporterInput(new SimpleExporterInput(jp));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(path));
			exporter.exportReport();
			logger.info("Excel File exported");

		} catch (FileNotFoundException | JRException | SQLException e) {

			e.printStackTrace();
		}

		outputFile = new File(path);

		return outputFile;
	}

	public String addAccessAndRole(BLRS_Access_Role_Entity access_Role_Entity, String user) {
		String msg = "";

		if (Objects.nonNull(access_Role_Entity.getUserId())) {
			access_Role_Entity.setEntry_user(user);
			access_Role_Entity.setEntity_flg("N");
			access_Role_Entity.setEntry_time(new Date());

			access_Role_Repo.save(access_Role_Entity);
			msg = "Access added ";
		} else {

			msg = "Access not added ";
		}

		return msg;
	}

	public String changePassword(String oldpass, String newpass, String userid) {

		String msg = "";
		Optional<BLRS_UserProfile_Entity> up = userProfileRep.findById(userid);
		String encrypted_password = null;
		try {
			encrypted_password = AES.encrypt(oldpass);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BLRS_UserProfile_Entity user = up.get();
		System.out.println(encrypted_password + " old pas : " + oldpass + " new pas : " + newpass + " pas : "
				+ user.getPassword());

		if (encrypted_password.equals(user.getPassword())) {
			if (up.isPresent()) {

				// if (PasswordEncryption.validatePassword(encrypted_password,
				// user.getPassword())) {

				// if (!PasswordEncryption.validatePassword(newpass, user.getPassword())) {

				String encryptedPassword = null;
				try {
					encryptedPassword = AES.encrypt(newpass);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(encryptedPassword + "new one");
				if (encryptedPassword.equals(user.getPassword())) {
					return msg = "New password cannot be Same as Old password";
				}

				user.setPassword(encryptedPassword);
				// user.setDel_flg("Y");

//						final Calendar cal = Calendar.getInstance();
//						cal.add(Calendar.MONTH,1);
//						System.out.println(cal.getTime());
//						user.setPw_expy_date(cal.getTime());

				userProfileRep.save(user);

				msg = "Password Changed Successfully";

			} else {

				msg = "User Not Found";
			}

		} else {
			msg = "Incorrect Old Password!";
		}

		return msg;
	};

}
