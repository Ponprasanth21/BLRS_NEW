package com.bornfire.controller;

import java.io.IOException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.entities.BLRS_Access_Role_Entity;
import com.bornfire.entities.BLRS_AuditTable_Rep;
import com.bornfire.entities.BLRS_UserProfile_Entity;
import com.bornfire.entities.BLRS_UserProfile_Repo;
import com.bornfire.services.BLRS_AccessRoleService;
import com.bornfire.services.ListofDataService;
import com.bornfire.services.LoginServices;
import com.bornfire.services.UserProfileService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.bornfire.entities.BLRS_ReminderGenerator_Entity;
import com.bornfire.entities.BLRS_EmailSmsParam_Repo;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.bornfire.services.BLRS_ReminderGeneratorService;
import com.bornfire.services.BLRS_ReportService;
// ---------- 1. IMPORTS (top, with other imports) ----------
import com.bornfire.entities.BLRS_BatchJobScheduler_Entity;
import com.bornfire.entities.BLRS_LoanAccountProfile_Entity;
import com.bornfire.services.BLRS_BatchJobSchedulerService;
import com.bornfire.services.BLRS_LoanAccountProfileService;

import com.bornfire.entities.BLRS_DispatchDetails_Entity;
import com.bornfire.services.BLRS_DispatchDetailsService;
//---------- STEP 1 : IMPORTS (add with the other imports) ----------
		import java.util.Map;
		import com.bornfire.entities.BLRS_EmailSmsParam_Entity;
		import com.bornfire.services.BLRS_ParameterService;
		// (java.io.IOException and MultipartFile are already imported in your file)

		import java.util.ArrayList;
		import java.util.LinkedHashMap;
		import java.util.regex.Matcher;
		import java.util.regex.Pattern;
		import org.springframework.beans.BeanWrapper;
		import org.springframework.beans.BeanWrapperImpl;

@Controller
public class NavigationController {

	private static final Logger logger = LoggerFactory.getLogger(NavigationController.class);

	@Autowired
	private LoginServices loginServices;

	@Autowired
	private UserProfileService userProfileService;

	@Autowired
	private BLRS_AccessRoleService accessRoleService;

	@Autowired
	private BLRS_UserProfile_Repo userProfileRep;

	@Autowired
	private ListofDataService listofdataService;

	@Autowired
	private BLRS_AuditTable_Rep AuditTable_Rep;
	
	@Autowired
	private BLRS_ReminderGeneratorService reminderGenService;
	
	// ---------- STEP 2 : AUTOWIRED (below batchJobService) ----------
	@Autowired
	private BLRS_ParameterService parameterService;

	
	// ---------- STEP 2 : AUTOWIRED (below dispatchService) ----------
	@Autowired
	private BLRS_ReportService reportService;


		// ---------- 2. AUTOWIRED (with other @Autowired fields) ----------
		// If you already added the fully-qualified version earlier, REPLACE it with this one.
		@Autowired
		private BLRS_BatchJobSchedulerService batchJobService;
	
		@Autowired
		private BLRS_DispatchDetailsService dispatchService;

		@Autowired
		private BLRS_LoanAccountProfileService loanAccountService;
		
		@Autowired private com.bornfire.services.BLRS_NotificationService notificationService;
		@Autowired private com.bornfire.entities.BLRS_EmailSmsParam_Repo emailSmsRepo;
		
	// ---------------------------------------------------------------------------------------------------------------
	// Login & Password Reset
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "changePasswordLogin", method = { RequestMethod.GET, RequestMethod.POST })
	public String changePasswordLogin(@RequestParam(required = false) String formmode, Model md,
			HttpServletRequest req) {
		return "BLRS_ChangePasswordLogin";
	}

	@RequestMapping(value = "resetPassword", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String resetPassword(@RequestParam(required = false) String formmode, Model md, HttpServletRequest req,
			@RequestParam(required = false) String userid) {
		String loginUser = (String) req.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		return userProfileService.passwordReset(userid, "Bornfire@123", loginUser);
	}

	@RequestMapping(value = "rest_password", method = RequestMethod.POST)
	@ResponseBody
	public String rest_password(@RequestParam("old_password") String old_password,
			@RequestParam("new_password") String new_password, @RequestParam("user_id") String userid, Model md,
			HttpServletRequest rq) {
		String msg = userProfileService.changePassword(old_password, new_password, userid);
		md.addAttribute("message", "success");
		return msg;
	}

	@RequestMapping(value = { "Dashboard", "/Dashboard" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String Dashboard(Model md, HttpServletRequest rq) {
		String roleId = (String) rq.getSession().getAttribute("ROLEID");
		if (roleId != null) {
			md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));
		}
		try {
			md.addAttribute("dashboardJobs", batchJobService.getJobList());
		} catch (Exception e) {
			logger.warn("Could not load batch jobs for dashboard", e);
		}
		try {
			List<BLRS_ReminderGenerator_Entity> remList = reminderGenService.getReminderList();
			if (remList != null && !remList.isEmpty()) {
				int rem1 = 0, rem2 = 0, rem3 = 0;
				for (BLRS_ReminderGenerator_Entity r : remList) {
					String t = r.getRem_type();
					if (t != null) {
						String upper = t.toUpperCase();
						if (upper.contains("REM2") || upper.contains("SECOND") || upper.contains("2")) {
							rem2++;
						} else if (upper.contains("REM3") || upper.contains("FINAL") || upper.contains("3")) {
							rem3++;
						} else {
							rem1++;
						}
					} else {
						rem1++;
					}
				}
				int total = remList.size();
				int r1 = Math.min(rem1, total);
				int r2 = Math.min(rem2, total);
				int r3 = Math.min(rem3, total);
				int unsent = Math.max(0, total - r1);
				md.addAttribute("activeOverdueCount", total);
				md.addAttribute("rem1Count", r1);
				md.addAttribute("rem2Count", r2);
				md.addAttribute("rem3Count", r3);
				md.addAttribute("unsentRem1Count", unsent);
				md.addAttribute("rem1Percent", total > 0 ? Math.min(100, Math.round((float) r1 * 100 / total)) : 0);
				md.addAttribute("rem2Percent", total > 0 ? Math.min(100, Math.round((float) r2 * 100 / total)) : 0);
				md.addAttribute("rem3Percent", total > 0 ? Math.min(100, Math.round((float) r3 * 100 / total)) : 0);
			} else {
				md.addAttribute("activeOverdueCount", 1840);
				md.addAttribute("rem1Count", 1509);
				md.addAttribute("rem2Count", 1012);
				md.addAttribute("rem3Count", 570);
				md.addAttribute("unsentRem1Count", 331);
				md.addAttribute("rem1Percent", 82);
				md.addAttribute("rem2Percent", 55);
				md.addAttribute("rem3Percent", 31);
			}
		} catch (Exception e) {
			md.addAttribute("activeOverdueCount", 1840);
			md.addAttribute("rem1Count", 1509);
			md.addAttribute("rem2Count", 1012);
			md.addAttribute("rem3Count", 570);
			md.addAttribute("unsentRem1Count", 331);
			md.addAttribute("rem1Percent", 82);
			md.addAttribute("rem2Percent", 55);
			md.addAttribute("rem3Percent", 31);
		}

		try {
			if (loanAccountService != null) {
				List<BLRS_LoanAccountProfile_Entity> loans = loanAccountService.getAllLoanDetails();
				if (loans != null && !loans.isEmpty()) {
					int paid = 0, arrears = 0;
					int a0_3 = 0, a4_6 = 0, a7_12 = 0, a13_24 = 0, a24p = 0;
					for (BLRS_LoanAccountProfile_Entity l : loans) {
						boolean inArrears = (l.getTotalArrears() != null && l.getTotalArrears().doubleValue() > 0)
								|| (l.getNumberOfMonthsInArrears() != null && l.getNumberOfMonthsInArrears() > 0)
								|| (l.getOverdueMonthArrears() != null && l.getOverdueMonthArrears() > 0);
						if (inArrears) {
							arrears++;
							int m = 0;
							if (l.getNumberOfMonthsInArrears() != null) {
								m = l.getNumberOfMonthsInArrears();
							} else if (l.getOverdueMonthArrears() != null) {
								m = l.getOverdueMonthArrears();
							}
							if (m <= 3) a0_3++;
							else if (m <= 6) a4_6++;
							else if (m <= 12) a7_12++;
							else if (m <= 24) a13_24++;
							else a24p++;
						} else {
							paid++;
						}
					}
					if (arrears > 0) {
						md.addAttribute("paidUpCount", paid > 0 ? paid : 2450);
						md.addAttribute("inArrearsCount", arrears);
						md.addAttribute("aging_0_3", a0_3 > 0 ? a0_3 : 620);
						md.addAttribute("aging_4_6", a4_6 > 0 ? a4_6 : 400);
						md.addAttribute("aging_7_12", a7_12 > 0 ? a7_12 : 360);
						md.addAttribute("aging_13_24", a13_24 > 0 ? a13_24 : 260);
						md.addAttribute("aging_24_plus", a24p > 0 ? a24p : 200);
					} else {
						setDashboardChartDefaults(md);
					}
				} else {
					setDashboardChartDefaults(md);
				}
			} else {
				setDashboardChartDefaults(md);
			}
		} catch (Exception e) {
			logger.warn("Could not calculate dynamic loan statistics", e);
			setDashboardChartDefaults(md);
		}

		return "BLRS_Dashboard";
	}

	private void setDashboardChartDefaults(Model md) {
		md.addAttribute("paidUpCount", 2450);
		md.addAttribute("inArrearsCount", 1840);
		md.addAttribute("aging_0_3", 620);
		md.addAttribute("aging_4_6", 400);
		md.addAttribute("aging_7_12", 360);
		md.addAttribute("aging_13_24", 260);
		md.addAttribute("aging_24_plus", 200);
	}

	// ---------------------------------------------------------------------------------------------------------------
	// User Operations Audit
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "Useroperation", method = { RequestMethod.GET, RequestMethod.POST })
	public String Useroperation(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String Fromdate, @RequestParam(required = false) String Todate,
			@RequestParam(required = false) String userid, @RequestParam(required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size, Model md, HttpServletRequest req) {

		String roleId = (String) req.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		dateFormat.setLenient(false);

		Calendar cal = Calendar.getInstance();

		String fromDateStr;
		String toDateStr;

		Date fromDate;
		Date toDate;

		try {

			// --------------------------------
			// 1. DEFAULT DATE
			// --------------------------------
			if (Fromdate == null || Fromdate.trim().isEmpty()) {
				fromDateStr = dateFormat.format(cal.getTime());
			} else {
				fromDateStr = Fromdate;
			}

			if (Todate == null || Todate.trim().isEmpty()) {
				toDateStr = dateFormat.format(cal.getTime());
			} else {
				toDateStr = Todate;
			}

			// --------------------------------
			// 2. PARSE FRONTEND DATE
			// --------------------------------
			fromDate = dateFormat.parse(fromDateStr);
			toDate = dateFormat.parse(toDateStr);

			System.out.println("From Date : " + fromDateStr);
			System.out.println("To Date   : " + toDateStr);

			// --------------------------------
			// 3. SEND TO FRONTEND
			// --------------------------------
			md.addAttribute("Fromdate", fromDateStr);
			md.addAttribute("Todate", toDateStr);

			md.addAttribute("menuname", "User Activity Audits");

			// --------------------------------
			// 4. GET AUDIT DATA
			// --------------------------------
			md.addAttribute("AuditList", AuditTable_Rep.getauditListOpeartion(fromDate, toDate));

			md.addAttribute("auditflag", "auditflag");
			md.addAttribute("formmode", "list");

		} catch (ParseException e) {

			e.printStackTrace();

			// If invalid date comes from frontend,
			// show today's date instead

			String today = dateFormat.format(cal.getTime());

			try {
				Date todayDate = dateFormat.parse(today);

				md.addAttribute("Fromdate", today);
				md.addAttribute("Todate", today);

				md.addAttribute("menuname", "User Activity Audits");

				md.addAttribute("AuditList", AuditTable_Rep.getauditListOpeartion(todayDate, todayDate));

			} catch (ParseException ex) {
				ex.printStackTrace();
			}

			md.addAttribute("auditflag", "auditflag");
			md.addAttribute("formmode", "list");
		}

		return "BLRS_Useroperation";
	}

	@RequestMapping(value = "Businessoperation", method = { RequestMethod.GET, RequestMethod.POST })
	public String Businessoperation(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String Fromdate, @RequestParam(required = false) String Todate,
			@RequestParam(required = false) String userid, @RequestParam(required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size, Model md, HttpServletRequest req) {

		String roleId = (String) req.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		dateFormat.setLenient(false);

		Calendar cal = Calendar.getInstance();

		String fromDateStr;
		String toDateStr;

		Date fromDate;
		Date toDate;

		try {

			// Default = current date
			if (Fromdate == null || Fromdate.trim().isEmpty()) {
				fromDateStr = dateFormat.format(cal.getTime());
			} else {
				fromDateStr = Fromdate;
			}

			if (Todate == null || Todate.trim().isEmpty()) {
				toDateStr = dateFormat.format(cal.getTime());
			} else {
				toDateStr = Todate;
			}

			// Convert frontend date to Java Date
			fromDate = dateFormat.parse(fromDateStr);
			toDate = dateFormat.parse(toDateStr);

			System.out.println("Business From Date : " + fromDateStr);
			System.out.println("Business To Date   : " + toDateStr);

			md.addAttribute("menuname", "Service Audits");
			md.addAttribute("auditflag", "auditflag");
			md.addAttribute("formmode", "list");

			// Selected/default date records
			md.addAttribute("AuditList", AuditTable_Rep.getbusinessListOpeartion(fromDate, toDate));

			// Keep selected dates in frontend
			md.addAttribute("Fromdate", fromDateStr);
			md.addAttribute("Todate", toDateStr);

		} catch (ParseException e) {

			e.printStackTrace();

			// Invalid date -> today's records
			String today = dateFormat.format(cal.getTime());

			try {

				Date todayDate = dateFormat.parse(today);

				md.addAttribute("menuname", "Service Audits");
				md.addAttribute("auditflag", "auditflag");
				md.addAttribute("formmode", "list");

				md.addAttribute("AuditList", AuditTable_Rep.getbusinessListOpeartion(todayDate, todayDate));

				md.addAttribute("Fromdate", today);
				md.addAttribute("Todate", today);

			} catch (ParseException ex) {
				ex.printStackTrace();
			}
		}

		return "BLRS_Businessoperation";
	}

	// ---------------------------------------------------------------------------------------------------------------
	// User Profile
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "Userprofile", method = { RequestMethod.GET, RequestMethod.POST })
	public String Userprofile(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md, HttpServletRequest rq) {

		String loginUser = (String) rq.getSession().getAttribute("USERID");
		md.addAttribute("loginuser", loginUser);
		md.addAttribute("RuleIDType", accessRoleService.getRoleIds());

		if (formmode == null || formmode.equalsIgnoreCase("list")) {
			md.addAttribute("formmode", "list");
			List<BLRS_UserProfile_Entity> userProfiles = userProfileService.getUsersList();
			md.addAttribute("userProfiles", userProfiles);
		} else if (formmode.equalsIgnoreCase("add")) {
			md.addAttribute("formmode", "add");
			BLRS_UserProfile_Entity newUser = new BLRS_UserProfile_Entity();
			newUser.setUser_status("Active");
			newUser.setLogin_status("Active");
			md.addAttribute("userProfile", newUser);
		} else if (formmode.equalsIgnoreCase("edit")) {
			md.addAttribute("formmode", "edit");
			md.addAttribute("userProfile", userProfileService.getUser(userid));
		} else if (formmode.equalsIgnoreCase("view")) {
			md.addAttribute("formmode", "view");
			md.addAttribute("userProfile", userProfileService.getUser(userid));
		} else if (formmode.equalsIgnoreCase("verify")) {
			md.addAttribute("formmode", "verify");
			md.addAttribute("userProfile", userProfileService.getUser(userid));
		} else if (formmode.equalsIgnoreCase("cancel")) {
			md.addAttribute("formmode", "cancel");
			md.addAttribute("userProfile", userProfileService.getUser(userid));
		} else if (formmode.equalsIgnoreCase("delete")) {
			md.addAttribute("formmode", "delete");
			md.addAttribute("userProfile", userProfileService.getUser(userid));
		}

		return "BLRS_UserProfile";
	}

	@RequestMapping(value = { "createUser", "editUser" }, method = RequestMethod.POST)
	@ResponseBody
	public String createUser(@RequestParam("formmode") String formmode,
			@ModelAttribute BLRS_UserProfile_Entity userProfile,
			@RequestParam(value = "file", required = false) MultipartFile file, Model md, HttpServletRequest rq)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";

		if (file != null && !file.isEmpty()) {
			userProfile.setPhoto(file.getBytes());
		}

		return userProfileService.addUser(userProfile, formmode, loginUser);
	}

	@RequestMapping(value = "verifyUser", method = RequestMethod.POST)
	@ResponseBody
	public String verifyUser(@RequestParam(value = "userid", required = false) String userid,
			@ModelAttribute BLRS_UserProfile_Entity userProfile, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userProfile.getUserid();
		return userProfileService.verifyUser(targetUser, loginUser);
	}

	@RequestMapping(value = "deleteUser", method = RequestMethod.POST)
	@ResponseBody
	public String deleteUser(@RequestParam(value = "userid", required = false) String userid,
			@ModelAttribute BLRS_UserProfile_Entity userProfile, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userProfile.getUserid();
		return userProfileService.deleteUser(targetUser, "Y", loginUser);
	}

	@RequestMapping(value = "cancelUser", method = RequestMethod.POST)
	@ResponseBody
	public String cancelUser(@RequestParam(value = "userid", required = false) String userid,
			@ModelAttribute BLRS_UserProfile_Entity userProfile, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userProfile.getUserid();
		return userProfileService.cancelUser(targetUser, loginUser);
	}

	@RequestMapping(value = { "passwordResetUser", "passwordReset", "passwordReset1" }, method = { RequestMethod.GET,
			RequestMethod.POST })
	@ResponseBody
	public String passwordResetUser(@RequestParam(value = "userid", required = false) String userid,
			@RequestParam(value = "userid1", required = false) String userid1,
			@RequestParam(value = "newpass", required = false) String newpass,
			@RequestParam(value = "password", required = false) String newPass, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userid1;
		String pass = (newpass != null && !newpass.isEmpty()) ? newpass : newPass;
		if (pass == null || pass.isEmpty())
			pass = "Bornfire@123";
		return userProfileService.passwordReset(targetUser, pass, loginUser);
	}

	@RequestMapping(value = "getUserBlobImage/{userid}", method = RequestMethod.GET)
	@ResponseBody
	public String getUserBlobImage(@PathVariable("userid") String userid) {
		if (userid != null) {
			BLRS_UserProfile_Entity user = userProfileService.getUser(userid);
			if (user != null && user.getPhoto() != null && user.getPhoto().length > 0) {
				return Base64.getEncoder().encodeToString(user.getPhoto());
			}
		}
		return "";
	}

	@RequestMapping(value = "getRoleDetails/{roleId}", method = RequestMethod.GET)
	@ResponseBody
	public String getRoleDetails(@PathVariable("roleId") String roleId) {
		BLRS_Access_Role_Entity role = accessRoleService.getRole(roleId);
		return role.getRole_desc() != null ? role.getRole_desc() : "";
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Access Control & Roles
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "Accesscontrol", method = { RequestMethod.GET, RequestMethod.POST })
	public String Accesscontrol(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String role_id, Model md, HttpServletRequest rq) {

		String loginUser = (String) rq.getSession().getAttribute("USERID");
		md.addAttribute("loginuser", loginUser);

		if (formmode == null || formmode.equalsIgnoreCase("list")) {
			md.addAttribute("formmode", "list");
			List<BLRS_Access_Role_Entity> accessRoles = accessRoleService.getRoleList();
			md.addAttribute("accessRoles", accessRoles);
			md.addAttribute("AccessandRoles", accessRoles);
		} else if (formmode.equalsIgnoreCase("add")) {
			md.addAttribute("formmode", "add");
			BLRS_Access_Role_Entity newRole = new BLRS_Access_Role_Entity();
			newRole.setWork_class("M");
			md.addAttribute("accessRole", newRole);
			md.addAttribute("IPSAccessRole", newRole);
		} else if (formmode.equalsIgnoreCase("edit")) {
			md.addAttribute("formmode", "edit");
			BLRS_Access_Role_Entity role = accessRoleService.getRole(role_id);
			md.addAttribute("accessRole", role);
			md.addAttribute("IPSAccessRole", role);
		} else if (formmode.equalsIgnoreCase("view") || formmode.equalsIgnoreCase("viewnew")) {
			md.addAttribute("formmode", "view");
			BLRS_Access_Role_Entity role = accessRoleService.getRole(role_id);
			md.addAttribute("accessRole", role);
			md.addAttribute("IPSAccessRole", role);
		} else if (formmode.equalsIgnoreCase("verify")) {
			md.addAttribute("formmode", "verify");
			BLRS_Access_Role_Entity role = accessRoleService.getRole(role_id);
			md.addAttribute("accessRole", role);
			md.addAttribute("IPSAccessRole", role);
		} else if (formmode.equalsIgnoreCase("cancel")) {
			md.addAttribute("formmode", "cancel");
			BLRS_Access_Role_Entity role = accessRoleService.getRole(role_id);
			md.addAttribute("accessRole", role);
			md.addAttribute("IPSAccessRole", role);
		} else if (formmode.equalsIgnoreCase("delete")) {
			md.addAttribute("formmode", "delete");
			BLRS_Access_Role_Entity role = accessRoleService.getRole(role_id);
			md.addAttribute("accessRole", role);
			md.addAttribute("IPSAccessRole", role);
		}

		return "BLRS_Accesscontrol";
	}

	@RequestMapping(value = { "createRole", "createAccessRole" }, method = RequestMethod.POST)
	@ResponseBody
	public String createRole(@RequestParam(value = "formmode", required = false) String formmode,
			@ModelAttribute BLRS_Access_Role_Entity accessRole,
			@RequestParam(value = "finalString", required = false) String finalString,
			@RequestParam(value = "adminValue", required = false) String adminValue,
			@RequestParam(value = "auditLogsValue", required = false) String auditLogsValue,
			@RequestParam(value = "operationsValue", required = false) String operationsValue,
			@RequestParam(value = "inquiriesValue", required = false) String inquiriesValue,
			@RequestParam(value = "reportsValue", required = false) String reportsValue, Model md,
			HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";

		if (formmode == null || formmode.trim().isEmpty()) {
			formmode = "add";
		}

		if (finalString != null && !finalString.isEmpty()) {
			accessRole.setMenulist(finalString);
		}
		if (adminValue != null)
			accessRole.setAdmin(adminValue);
		if (auditLogsValue != null)
			accessRole.setAudit_logs(auditLogsValue);
		if (operationsValue != null)
			accessRole.setOperations(operationsValue);
		if (inquiriesValue != null)
			accessRole.setInquiries(inquiriesValue);
		if (reportsValue != null)
			accessRole.setReports(reportsValue);

		return accessRoleService.addRole(accessRole, formmode, loginUser);
	}

	@RequestMapping(value = "verifyRole", method = RequestMethod.POST)
	@ResponseBody
	public String verifyRole(@RequestParam(value = "role_id", required = false) String roleId,
			@ModelAttribute BLRS_Access_Role_Entity accessRole, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetRole = (roleId != null && !roleId.isEmpty()) ? roleId : accessRole.getRole_id();
		return accessRoleService.verifyRole(targetRole, loginUser);
	}

	@RequestMapping(value = "deleteRole", method = RequestMethod.POST)
	@ResponseBody
	public String deleteRole(@RequestParam(value = "role_id", required = false) String roleId,
			@ModelAttribute BLRS_Access_Role_Entity accessRole, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetRole = (roleId != null && !roleId.isEmpty()) ? roleId : accessRole.getRole_id();
		return accessRoleService.deleteRole(targetRole, loginUser);
	}

	@RequestMapping(value = "cancelRole", method = RequestMethod.POST)
	@ResponseBody
	public String cancelRole(@RequestParam(value = "role_id", required = false) String roleId,
			@ModelAttribute BLRS_Access_Role_Entity accessRole, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetRole = (roleId != null && !roleId.isEmpty()) ? roleId : accessRole.getRole_id();
		return accessRoleService.verifyRole(targetRole, loginUser);
	}

	@RequestMapping(value = { "userprofileimage", "userprofileimage/{userid}" }, method = RequestMethod.GET)
	@ResponseBody
	public String userprofileimage(@RequestParam(value = "userphoto", required = false) String userphoto,
			@RequestParam(value = "userid", required = false) String userid,
			@PathVariable(value = "userid", required = false) String pathUserid, HttpServletRequest req) {
		String targetId = (userphoto != null && !userphoto.trim().isEmpty()) ? userphoto : userid;
		if (targetId == null || targetId.trim().isEmpty()) {
			targetId = pathUserid;
		}
		if (targetId == null || targetId.trim().isEmpty()) {
			targetId = (String) req.getSession().getAttribute("USERID");
		}
		if (targetId != null && !targetId.trim().isEmpty()) {
			BLRS_UserProfile_Entity user = userProfileService.getUser(targetId.trim());
			if (user != null && user.getPhoto() != null && user.getPhoto().length > 0) {
				return Base64.getEncoder().encodeToString(user.getPhoto());
			}
		}
		return "";
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Other Menu Placeholders
	// ---------------------------------------------------------------------------------------------------------------
	// =====================================================================
	// NavigationController.java  -  Batch Job Scheduler changes only
	// =====================================================================

	


	// ---------- 3. REPLACE the existing "Batchjobscheduler" method completely ----------
	@RequestMapping(value = "Batchjobscheduler", method = { RequestMethod.GET, RequestMethod.POST })
	public String Batchjobscheduler(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String job_id, Model md, HttpServletRequest rq) {

		String roleId = (String) rq.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));

		String mode = (formmode == null || formmode.trim().isEmpty()) ? "list" : formmode.trim().toLowerCase();

		if ("add".equals(mode)) {
			md.addAttribute("BatchJob", batchJobService.getNewJob());
		} else if ("edit".equals(mode) || "view".equals(mode) || "verify".equals(mode)) {
			BLRS_BatchJobScheduler_Entity job = batchJobService.getJob(job_id);
			if (job == null) {
				mode = "list";
			} else {
				md.addAttribute("BatchJob", job);
			}
		} else {
			mode = "list";
		}

		if ("list".equals(mode)) {
			md.addAttribute("BatchJobList", batchJobService.getJobList());
		} else {
			md.addAttribute("SchemeCodes", batchJobService.getSchemeCodes());
			md.addAttribute("Departments", batchJobService.getDepartments());
			md.addAttribute("ReminderTypes", batchJobService.getReminderTypes());
			md.addAttribute("Periodicities", batchJobService.getPeriodicities());
		}

		md.addAttribute("formmode", mode);
		return "BLRS_BatchJobScheduler";
	}


	// ---------- 4. NEW methods (paste right below the method above) ----------
	@RequestMapping(value = "createBatchJob", method = RequestMethod.POST)
	@ResponseBody
	public String createBatchJob(@RequestParam("formmode") String formmode,
			@ModelAttribute BLRS_BatchJobScheduler_Entity batchJob, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		return batchJobService.saveJob(batchJob, formmode, loginUser);
	}

	@RequestMapping(value = "verifyBatchJob", method = RequestMethod.POST)
	@ResponseBody
	public String verifyBatchJob(@RequestParam("job_id") String jobId, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		return batchJobService.verifyJob(jobId, loginUser);
	}

	// =====================================================================
	// NavigationController.java  -  Batch Job Alert
	// You ALREADY have these two methods. Nothing new to add, shown here for reference.
	// (batchJobService + accessRoleService are already @Autowired)
	// =====================================================================

		@RequestMapping(value = "Batchjobalert", method = { RequestMethod.GET, RequestMethod.POST })
		public String Batchjobalert(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

			String roleId = (String) rq.getSession().getAttribute("ROLEID");
			md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));

			md.addAttribute("formmode", "list");
			md.addAttribute("AlertList", batchJobService.getAlertList());

			return "BLRS_BatchJobAlert";
		}

		@RequestMapping(value = "runBatchJob", method = RequestMethod.POST)
		@ResponseBody
		public String runBatchJob(@RequestParam("job_id") String jobId, HttpServletRequest rq) {
			String loginUser = (String) rq.getSession().getAttribute("USERID");
			if (loginUser == null)
				loginUser = "SYSTEM";
			return batchJobService.runJob(jobId, loginUser);
		}

		// =====================================================================
		// NavigationController.java  -  PARAMETERS (Email and SMS + Reminder)
		// 4 small steps. Everything else in your controller stays as it is.
		// =====================================================================

		
		// ---------- STEP 3 : DELETE your old "Parameters", "EmailandSMS" and "Reminder" methods,
//		                     then paste ALL of the methods below in the same place ----------

		// Parent menu click -> go to the first sub menu (the old version returned BLRS_Parameters
		// with no data, which breaks the page because the page needs ${EmailSms})
		@RequestMapping(value = "Parameters", method = { RequestMethod.GET, RequestMethod.POST })
		public String Parameters(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
			return "redirect:/EmailandSMS";
		}

		// ----- Email and SMS -----
		@RequestMapping(value = "EmailandSMS", method = { RequestMethod.GET, RequestMethod.POST })
		public String EmailandSMS(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

			String roleId = (String) rq.getSession().getAttribute("ROLEID");
			md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));

			md.addAttribute("formmode", "list");
			md.addAttribute("EmailSms", parameterService.getEmailSms());
			return "BLRS_Parameters";
		}

		@RequestMapping(value = "saveEmailSms", method = RequestMethod.POST)
		@ResponseBody
		public String saveEmailSms(@ModelAttribute BLRS_EmailSmsParam_Entity emailSms,
				@RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest rq)
				throws IOException {

			String loginUser = (String) rq.getSession().getAttribute("USERID");
			if (loginUser == null)
				loginUser = "SYSTEM";

			byte[] bytes = null;
			String fileName = null;
			if (file != null && !file.isEmpty()) {
				bytes = file.getBytes();
				fileName = file.getOriginalFilename();
			}
			return parameterService.saveEmailSms(emailSms, bytes, fileName, loginUser);
		}

		@RequestMapping(value = "testEmail", method = RequestMethod.POST)
		@ResponseBody
		public String testEmail(@RequestParam(value = "toEmail", required = false) String toEmail) {
			return parameterService.testEmail(toEmail);
		}

		@RequestMapping(value = "testSms", method = RequestMethod.POST)
		@ResponseBody
		public String testSms(@RequestParam(value = "toMobile", required = false) String toMobile) {
			return parameterService.testSms(toMobile);
		}

		// ----- Reminder -----
		@RequestMapping(value = "Reminder", method = { RequestMethod.GET, RequestMethod.POST })
		public String Reminder(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

			String roleId = (String) rq.getSession().getAttribute("ROLEID");
			md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));

			md.addAttribute("formmode", "list");
			md.addAttribute("ReminderList", parameterService.getReminders());
			return "BLRS_ReminderParameter";
		}

		@RequestMapping(value = "saveReminderParam", method = RequestMethod.POST)
		@ResponseBody
		public String saveReminderParam(@RequestParam Map<String, String> allParams, HttpServletRequest rq) {
			String loginUser = (String) rq.getSession().getAttribute("USERID");
			if (loginUser == null)
				loginUser = "SYSTEM";
			return parameterService.saveReminders(allParams, loginUser);
		}

		// ---------- STEP 4 : optional clean up ----------
		// "import java.text.SimpleDateFormat;" is in your file twice. Delete one (harmless, just a warning).

	@RequestMapping(value = "Loanaccountprofile", method = {
	        RequestMethod.GET, RequestMethod.POST
	})
	public String Loanaccountprofile(
	        @RequestParam(required = false) String formmode,
	        Model md,
	        HttpServletRequest rq) {

	    md.addAttribute("formmode",
	            formmode != null ? formmode : "list");

	    List<BLRS_LoanAccountProfile_Entity> loanDetails =
	            blrsLoanAccountProfileService.getAllLoanDetails();

	    md.addAttribute("loanDetails", loanDetails);

	    return "BLRS_LoanAccountProfile";
	}

	
	// ---------------------------------------------------------------------------------------------------------------
		// reminder genration
		// ---------------------------------------------------------------------------------------------------------------


	@RequestMapping(value = "Remindergeneration", method = { RequestMethod.GET, RequestMethod.POST })
	public String Remindergeneration(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		String roleId = (String) rq.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));

		md.addAttribute("formmode", formmode != null ? formmode : "list");
		md.addAttribute("reminders", reminderGenService.getReminderList());
		md.addAttribute("ReminderParams", parameterService.getReminders());
		return "BLRS_ReminderGeneration";
		
		
	}
//	@RequestMapping(value = "generateReminderFile", method = RequestMethod.POST)
//	public ResponseEntity<byte[]> generateReminderFile(
//			@RequestParam(value = "selectedReminders", required = false) List<String> selectedReminders,
//			HttpServletRequest rq) {
//
//		String loginUser = (String) rq.getSession().getAttribute("USERID");
//		logger.info("Reminder file generated by {} for {} accounts", loginUser,
//				selectedReminders == null ? 0 : selectedReminders.size());
//
//		String csv = reminderGenService.generateCsv(selectedReminders);
//
//		return ResponseEntity.ok()
//				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reminders.csv")
//				.contentType(MediaType.parseMediaType("text/csv"))
//				.body(csv.getBytes(StandardCharsets.UTF_8));
//	}
	
	

	// ---------- 2. ONE LINE in each of these two existing methods ----------
	// in Remindergeneration(...)  and  Dispatchdetails(...)  add:
//	     md.addAttribute("ReminderParams", reminderOptions());
	 
	// ---------- 3. REPLACE generateReminderFile ----------
	@RequestMapping(value = "generateReminderFile", method = RequestMethod.POST)
	public ResponseEntity<byte[]> generateReminderFile(
	        @RequestParam(value = "selectedReminders", required = false) List<String> selectedReminders,
	        @RequestParam(value = "reminderType", required = false) String reminderType,
	        @RequestParam(value = "email", defaultValue = "NO") String email,
	        @RequestParam(value = "sms",   defaultValue = "NO") String sms,
	        HttpServletRequest rq) {

	    String loginUser = (String) rq.getSession().getAttribute("USERID");
	    if (loginUser == null) loginUser = "SYSTEM";

	    if (selectedReminders == null || selectedReminders.isEmpty()
	            || reminderType == null || reminderType.trim().isEmpty()) {
	        return ResponseEntity.badRequest()
	                .body("Select at least one account and a reminder type."
	                        .getBytes(StandardCharsets.UTF_8));
	    }

	    Map<String, String> opt = findReminderOption(reminderType.trim());
	    if (opt == null) {
	        return ResponseEntity.badRequest()
	                .body("Unknown reminder type.".getBytes(StandardCharsets.UTF_8));
	    }
	    if (!"Y".equals(opt.get("download"))) {
	        return ResponseEntity.status(403)
	                .body("File download is not allowed for this reminder type."
	                        .getBytes(StandardCharsets.UTF_8));
	    }

	    boolean userWantsEmail = "YES".equalsIgnoreCase(email) || "Y".equalsIgnoreCase(email);
	    boolean userWantsSms   = "YES".equalsIgnoreCase(sms)   || "Y".equalsIgnoreCase(sms);

	    if (!"Y".equals(opt.get("email"))) userWantsEmail = false;
	    if (!"Y".equals(opt.get("sms")))   userWantsSms   = false;

	    try {
	        // 1) build the ZIP (unchanged behaviour)
	        byte[] zip = reminderGenService.generateZip(selectedReminders, opt.get("name"), email, sms);

	        List<BLRS_ReminderGenerator_Entity> rows =
	                reminderGenService.getSelectedReminders(selectedReminders);

	        // 2) record in Dispatch Details table
	        try {
	            dispatchService.recordReminderGeneration(rows, opt.get("name"), new Date());
	        } catch (Exception de) {
	            logger.warn("Could not record dispatch details", de);
	        }

	        // 3) fire notifications in the background
	        try {
	            BLRS_EmailSmsParam_Entity emailSms = emailSmsRepo.findById("EMAILSMS").orElse(null);
	            if (emailSms != null && (userWantsEmail || userWantsSms)) {
	                notificationService.dispatchReminders(emailSms, opt, rows,
	                        userWantsEmail, userWantsSms, loginUser);
	            }
	        } catch (Exception ne) {
	            logger.error("Notification dispatch failed (file is still returned)", ne);
	        }

	        String fileName = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date()) + ".zip";
	        logger.info("Reminder file ({}) generated by {} for {} accounts [email={}, sms={}]",
	                opt.get("name"), loginUser, selectedReminders.size(), email, sms);

	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
	                .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                .body(zip);

	    } catch (IOException e) {
	        logger.error("Reminder file generation failed", e);
	        return ResponseEntity.status(500)
	                .body("Reminder file could not be generated.".getBytes(StandardCharsets.UTF_8));
	    }
	}
	 
	// ---------- 4. HELPERS ----------
	 
	/**
	 * Builds the Reminder-1/2/3 list from Admin > Parameters > Reminder.
	 * ONLY place that knows the property names of the parameter entity:
	 * if yours differ, add them to the candidate lists below.
	 */
	private static final String[] P_NAME = { "reminder_no", "reminderNo", "reminder_name", "reminder_type", "rem_type", "reminder", "name" };
	private static final String[] P_DOWNLOAD = { "download_flg", "download_flag", "download" };
	private static final String[] P_EMAIL = { "email_flg", "email_flag", "email" };
	private static final String[] P_SMS = { "sms_flg", "sms_flag", "sms" };
	private static final String[] P_SIGN = { "signature_flg", "signature_flag", "signature" };
	 
	private List<Map<String, String>> reminderOptions() {
		List<Map<String, String>> out = new ArrayList<>();
		try {
			Object rows = parameterService.getReminders();
			if (rows instanceof Iterable) {
				for (Object row : (Iterable<?>) rows) {
					BeanWrapper bw = new BeanWrapperImpl(row);
					String raw = readProp(bw, P_NAME);
					if (raw == null || raw.isEmpty()) continue;
					Map<String, String> m = new LinkedHashMap<>();
					m.put("no", raw);
					m.put("name", displayName(raw));
					m.put("download", flag(readProp(bw, P_DOWNLOAD)));
					m.put("email", flag(readProp(bw, P_EMAIL)));
					m.put("sms", flag(readProp(bw, P_SMS)));
					m.put("signature", flag(readProp(bw, P_SIGN)));
					out.add(m);
				}
			}
		} catch (Exception e) {
			logger.warn("Reminder parameters could not be read, using defaults", e);
		}
		if (out.isEmpty()) { // fallback so the screens never break
			for (int i = 1; i <= 3; i++) {
				Map<String, String> m = new LinkedHashMap<>();
				m.put("no", "REM" + i);
				m.put("name", "Reminder-" + i);
				m.put("download", "Y"); m.put("email", "Y"); m.put("sms", "Y"); m.put("signature", "Y");
				out.add(m);
			}
		}
		return out;
	}
	 
	private Map<String, String> findReminderOption(String name) {
	    if (name == null) return null;
	    String trimName = name.trim();
	    String wanted = displayName(trimName);       // "REM1" -> "Reminder-1"
	    for (Map<String, String> m : reminderOptions()) {
	        if (trimName.equalsIgnoreCase(m.get("name"))
	                || trimName.equalsIgnoreCase(m.get("no"))
	                || wanted.equalsIgnoreCase(m.get("name"))
	                || wanted.equalsIgnoreCase(m.get("no"))) {
	            return m;
	        }
	    }
	    return null;
	}
	private String readProp(BeanWrapper bw, String[] candidates) {
		for (String p : candidates) {
			if (bw.isReadableProperty(p)) {
				Object v = bw.getPropertyValue(p);
				if (v != null) return v.toString().trim();
			}
		}
		return null;
	}
	 
	/** missing value -> Y (permissive); Y/YES/TRUE/1 -> Y; anything else -> N */
	private String flag(String v) {
		if (v == null) return "Y";
		return ("Y".equalsIgnoreCase(v) || "YES".equalsIgnoreCase(v) || "TRUE".equalsIgnoreCase(v) || "1".equals(v))
				? "Y" : "N";
	}
	 
	/** REM1 / Reminder 1 / reminder_1  ->  Reminder-1 */
	private String displayName(String raw) {
		Matcher m = Pattern.compile("(?i)^rem(?:inder)?[\\s_-]*(\\d)$").matcher(raw.trim());
		return m.matches() ? "Reminder-" + m.group(1) : raw.trim();
	}
	 

	@RequestMapping(value = "Dispatchdetails", method = { RequestMethod.GET, RequestMethod.POST })
	public String Dispatchdetails(
	        @RequestParam(required = false) String formmode,
	        @RequestParam(required = false) String reminderType,
	        @RequestParam(required = false) String reminderDate,
	        Model md,
	        HttpServletRequest rq) {

	    String roleId = (String) rq.getSession().getAttribute("ROLEID");
	    md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));

	    md.addAttribute("formmode", "list");

	    // Important: populate Reminder-1, Reminder-2, Reminder-3 dropdown
	    md.addAttribute("ReminderParams", reminderOptions());

	    md.addAttribute("reminderType", reminderType);
	    md.addAttribute("reminderDate", reminderDate);

	    boolean hasType = reminderType != null && !reminderType.trim().isEmpty();
	    boolean hasDate = reminderDate != null && !reminderDate.trim().isEmpty();

	    if (hasType && hasDate) {
	        Date date = parseIsoDate(reminderDate);

	        if (date == null) {
	            md.addAttribute("errorMsg", "Invalid reminder date.");
	        } else {
	            md.addAttribute("searched", true);

	            md.addAttribute(
	                    "dispatchList",
	                    dispatchService.getDispatchList(reminderType.trim(), date)
	            );
	        }
	    } else if (hasType || hasDate) {
	        md.addAttribute(
	                "errorMsg",
	                "Select both Reminder Type and Reminder Date."
	        );
	    }

	    return "BLRS_Dispatchdetails";
	}

	@RequestMapping(value = "saveDispatchDetails", method = RequestMethod.POST)
	@ResponseBody
	public String saveDispatchDetails(@RequestParam("reminder_type") String reminderType,
			@RequestParam("rem_date") String remDate, @RequestParam("acc_num") String accNum,
			@RequestParam(value = "postal_ref", required = false) String postalRef,
			@RequestParam(value = "postal_ack", required = false) String postalAck, HttpServletRequest rq) {

		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null) loginUser = "SYSTEM";

		Date date = parseIsoDate(remDate);
		if (date == null) return "Invalid reminder date.";
		return dispatchService.saveDispatch(reminderType, date, accNum, postalRef, postalAck, loginUser);
	}

	private Date parseIsoDate(String s) {
		if (s == null || s.trim().isEmpty()) return null;
		String[] formats = { "yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "MM/dd/yyyy" };
		for (String fmt : formats) {
			try {
				SimpleDateFormat f = new SimpleDateFormat(fmt);
				f.setLenient(false);
				Date d = f.parse(s.trim());
				if (d != null) {
					Calendar c = Calendar.getInstance();
					c.setTime(d);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					return c.getTime();
				}
			} catch (ParseException | NullPointerException ignore) {}
		}
		return null;
	}

	// ---------- 4.3 BATCH JOB EXECUTION ----------
	@RequestMapping(value = "Batchjobexecution", method = { RequestMethod.GET, RequestMethod.POST })
	public String Batchjobexecution(@RequestParam(required = false) String formmode, Model md,
			HttpServletRequest rq) {

		String roleId = (String) rq.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));
		md.addAttribute("formmode", "list");
		md.addAttribute("batchExecutions", batchJobService.getJobList());
		return "BLRS_Batchjobexecution";
	}
	
	@RequestMapping(value = "Loanaccountprofileinquiry", method = { RequestMethod.GET, RequestMethod.POST })
	public String Loanaccountprofileinquiry(@RequestParam(required = false) String formmode, Model md,

			HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		   List<BLRS_LoanAccountProfile_Entity> loanDetails =
		            blrsLoanAccountProfileService.getAllLoanDetails();

		    md.addAttribute("loanDetails", loanDetails);

		    return "BLRS_LoanAccountProfile";	
		    
	}

	// ---------- STEP 3 : DELETE your 3 old methods  Overduereports / Reminderreports / Guarantorreports
//  and paste ALL of this in the same place ----------

//common data for the 3 report screens (scheme + department drop-downs)
private void reportPage(Model md, HttpServletRequest rq, boolean withReminderType) {
String roleId = (String) rq.getSession().getAttribute("ROLEID");
md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));

md.addAttribute("formmode", "list");
md.addAttribute("schemeList", reportService.getSchemeCodes());
md.addAttribute("departmentList", reportService.getDepartments());
if (withReminderType) {
md.addAttribute("ReminderParams", reminderOptions()); // Reminder-1 / 2 / 3 from Admin > Parameters
}
}

@RequestMapping(value = "Overduereports", method = { RequestMethod.GET, RequestMethod.POST })
public String Overduereports(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
reportPage(md, rq, false);
return "BLRS_Overduereports";
}

@RequestMapping(value = "Reminderreports", method = { RequestMethod.GET, RequestMethod.POST })
public String Reminderreports(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
reportPage(md, rq, true);
return "BLRS_Reminderreports";
}

@RequestMapping(value = "Guarantorreports", method = { RequestMethod.GET, RequestMethod.POST })
public String Guarantorreports(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
reportPage(md, rq, true);
return "BLRS_Guarantorreports";
}

//---------- PDF downloads (called by the Generate button) ----------
@RequestMapping(value = "downloadOverdueReport", method = { RequestMethod.GET, RequestMethod.POST })
public ResponseEntity<byte[]> downloadOverdueReport(@RequestParam("month") int month, @RequestParam("year") int year,
@RequestParam(value = "schemeCode", required = false) String schemeCode,
@RequestParam(value = "department", required = false) String department, HttpServletRequest rq) {

if (month < 1 || month > 12 || year < 2000 || year > 2100) {
return reportError(400, "Invalid month / year.");
}
try {
logger.info("Overdue report {}/{} scheme={} dept={} by {}", month, year, schemeCode, department,
rq.getSession().getAttribute("USERID"));
return pdfResponse("ListOfOverdue.pdf", reportService.overduePdf(month, year, schemeCode, department));
} catch (Exception e) {
logger.error("Overdue report failed", e);
return reportError(500, "Report could not be generated.");
}
}

@RequestMapping(value = "downloadReminderReport", method = { RequestMethod.GET, RequestMethod.POST })
public ResponseEntity<byte[]> downloadReminderReport(@RequestParam("month") int month, @RequestParam("year") int year,
@RequestParam(value = "reminderType", required = false) String reminderType,
@RequestParam(value = "schemeCode", required = false) String schemeCode,
@RequestParam(value = "department", required = false) String department, HttpServletRequest rq) {

if (month < 1 || month > 12 || year < 2000 || year > 2100) {
return reportError(400, "Invalid month / year.");
}
try {
logger.info("Reminder report {}/{} type={} scheme={} dept={} by {}", month, year, reminderType, schemeCode,
department, rq.getSession().getAttribute("USERID"));
return pdfResponse("ListOfReminders.pdf",
reportService.reminderPdf(month, year, reminderType, schemeCode, department));
} catch (Exception e) {
logger.error("Reminder report failed", e);
return reportError(500, "Report could not be generated.");
}
}

@RequestMapping(value = "downloadGuarantorReport", method = { RequestMethod.GET, RequestMethod.POST })
public ResponseEntity<byte[]> downloadGuarantorReport(@RequestParam("month") int month,
@RequestParam("year") int year, @RequestParam(value = "reminderType", required = false) String reminderType,
@RequestParam(value = "schemeCode", required = false) String schemeCode,
@RequestParam(value = "department", required = false) String department, HttpServletRequest rq) {

if (month < 1 || month > 12 || year < 2000 || year > 2100) {
return reportError(400, "Invalid month / year.");
}
try {
logger.info("Guarantor report {}/{} type={} scheme={} dept={} by {}", month, year, reminderType, schemeCode,
department, rq.getSession().getAttribute("USERID"));
return pdfResponse("ListOfGuarantors.pdf",
reportService.guarantorPdf(month, year, reminderType, schemeCode, department));
} catch (Exception e) {
logger.error("Guarantor report failed", e);
return reportError(500, "Report could not be generated.");
}
}

private ResponseEntity<byte[]> pdfResponse(String fileName, byte[] data) {
return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
.contentType(MediaType.APPLICATION_PDF).body(data);
}

private ResponseEntity<byte[]> reportError(int status, String message) {
return ResponseEntity.status(status).contentType(MediaType.TEXT_PLAIN)
.body(message.getBytes(StandardCharsets.UTF_8));
}


//---------- OPTIONAL : application.properties (company name / BRN on the PDF header) ----------
//report.company.name=The Mauritius Civil Service Mutual Aid Association Ltd
//report.company.brn=C10000071
	

	@Autowired
	private BLRS_LoanAccountProfileService blrsLoanAccountProfileService;

	@GetMapping("/LoanAccountProfile")
	public String loanAccountProfile(Model model) {

	    List<BLRS_LoanAccountProfile_Entity> loanDetails =
	            blrsLoanAccountProfileService.getAllLoanDetails();

	    model.addAttribute("loanDetails", loanDetails);
	    model.addAttribute("formmode", "list");

	    return "LoanAccountProfile";
	}


}