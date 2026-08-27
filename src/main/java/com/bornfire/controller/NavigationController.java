package com.bornfire.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Base64;
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
import com.bornfire.entities.BLRS_AuditTablePojo;
import com.bornfire.entities.BLRS_UserProfile_Entity;
import com.bornfire.entities.BLRS_UserProfile_Repo;
import com.bornfire.services.BLRS_AccessRoleService;
import com.bornfire.services.ListofDataService;
import com.bornfire.services.LoginServices;
import com.bornfire.services.UserProfileService;

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

	// ---------------------------------------------------------------------------------------------------------------
	// Login & Password Reset
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "changePasswordLogin", method = { RequestMethod.GET, RequestMethod.POST })
	public String changePasswordLogin(@RequestParam(required = false) String formmode, Model md, HttpServletRequest req) {
		return "BLRS_ChangePasswordLogin";
	}

	@RequestMapping(value = "resetPassword", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String resetPassword(@RequestParam(required = false) String formmode, Model md, HttpServletRequest req,
			@RequestParam(required = false) String userid) {
		String loginUser = (String) req.getSession().getAttribute("USERID");
		if (loginUser == null) loginUser = "SYSTEM";
		return loginServices.passwordReset(userid, "Bornfire@123", loginUser);
	}

	@RequestMapping(value = "rest_password", method = RequestMethod.POST)
	@ResponseBody
	public String rest_password(@RequestParam("old_password") String old_password,
			@RequestParam("new_password") String new_password, @RequestParam("user_id") String userid, Model md,
			HttpServletRequest rq) {
		String msg = loginServices.changePassword(old_password, new_password, userid);
		md.addAttribute("message", "success");
		return msg;
	}

	@GetMapping("/Dashboard")
	public String getMethodName(Model md) {
		return "BLRS_Dashboard";
	}

	// ---------------------------------------------------------------------------------------------------------------
	// User Operations Audit
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "Useroperation", method = { RequestMethod.GET, RequestMethod.POST })
	public String Useroperation(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_Useroperation";
	}

	@RequestMapping(value = "Businessoperation", method = { RequestMethod.GET, RequestMethod.POST })
	public String Businessoperation(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
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

	@RequestMapping(value = {"createUser", "editUser"}, method = RequestMethod.POST)
	@ResponseBody
	public String createUser(@RequestParam("formmode") String formmode,
			@ModelAttribute BLRS_UserProfile_Entity userProfile,
			@RequestParam(value = "file", required = false) MultipartFile file, Model md, HttpServletRequest rq)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null) loginUser = "SYSTEM";

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
		if (loginUser == null) loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userProfile.getUserid();
		return userProfileService.verifyUser(targetUser, loginUser);
	}

	@RequestMapping(value = "deleteUser", method = RequestMethod.POST)
	@ResponseBody
	public String deleteUser(@RequestParam(value = "userid", required = false) String userid,
			@ModelAttribute BLRS_UserProfile_Entity userProfile, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null) loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userProfile.getUserid();
		return userProfileService.deleteUser(targetUser, "Y", loginUser);
	}

	@RequestMapping(value = "cancelUser", method = RequestMethod.POST)
	@ResponseBody
	public String cancelUser(@RequestParam(value = "userid", required = false) String userid,
			@ModelAttribute BLRS_UserProfile_Entity userProfile, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null) loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userProfile.getUserid();
		return userProfileService.cancelUser(targetUser, loginUser);
	}

	@RequestMapping(value = {"passwordResetUser", "passwordReset", "passwordReset1"}, method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String passwordResetUser(@RequestParam(value = "userid", required = false) String userid,
			@RequestParam(value = "userid1", required = false) String userid1,
			@RequestParam(value = "newpass", required = false) String newpass,
			@RequestParam(value = "password", required = false) String newPass, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null) loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userid1;
		String pass = (newpass != null && !newpass.isEmpty()) ? newpass : newPass;
		if (pass == null || pass.isEmpty()) pass = "Bornfire@123";
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

	@RequestMapping(value = {"createRole", "createAccessRole"}, method = RequestMethod.POST)
	@ResponseBody
	public String createRole(@RequestParam(value = "formmode", required = false) String formmode,
			@ModelAttribute BLRS_Access_Role_Entity accessRole,
			@RequestParam(value = "finalString", required = false) String finalString,
			@RequestParam(value = "adminValue", required = false) String adminValue,
			@RequestParam(value = "auditLogsValue", required = false) String auditLogsValue,
			@RequestParam(value = "operationsValue", required = false) String operationsValue,
			@RequestParam(value = "inquiriesValue", required = false) String inquiriesValue,
			@RequestParam(value = "reportsValue", required = false) String reportsValue,
			Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null) loginUser = "SYSTEM";

		if (formmode == null || formmode.trim().isEmpty()) {
			formmode = "add";
		}

		if (finalString != null && !finalString.isEmpty()) {
			accessRole.setMenulist(finalString);
		}
		if (adminValue != null) accessRole.setAdmin(adminValue);
		if (auditLogsValue != null) accessRole.setAudit_logs(auditLogsValue);
		if (operationsValue != null) accessRole.setOperations(operationsValue);
		if (inquiriesValue != null) accessRole.setInquiries(inquiriesValue);
		if (reportsValue != null) accessRole.setReports(reportsValue);

		return accessRoleService.addRole(accessRole, formmode, loginUser);
	}

	@RequestMapping(value = "verifyRole", method = RequestMethod.POST)
	@ResponseBody
	public String verifyRole(@RequestParam(value = "role_id", required = false) String roleId,
			@ModelAttribute BLRS_Access_Role_Entity accessRole, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null) loginUser = "SYSTEM";
		String targetRole = (roleId != null && !roleId.isEmpty()) ? roleId : accessRole.getRole_id();
		return accessRoleService.verifyRole(targetRole, loginUser);
	}

	@RequestMapping(value = "deleteRole", method = RequestMethod.POST)
	@ResponseBody
	public String deleteRole(@RequestParam(value = "role_id", required = false) String roleId,
			@ModelAttribute BLRS_Access_Role_Entity accessRole, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null) loginUser = "SYSTEM";
		String targetRole = (roleId != null && !roleId.isEmpty()) ? roleId : accessRole.getRole_id();
		return accessRoleService.deleteRole(targetRole, loginUser);
	}

	@RequestMapping(value = "cancelRole", method = RequestMethod.POST)
	@ResponseBody
	public String cancelRole(@RequestParam(value = "role_id", required = false) String roleId,
			@ModelAttribute BLRS_Access_Role_Entity accessRole, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null) loginUser = "SYSTEM";
		String targetRole = (roleId != null && !roleId.isEmpty()) ? roleId : accessRole.getRole_id();
		return accessRoleService.verifyRole(targetRole, loginUser);
	}

	@RequestMapping(value = "userprofileimage", method = RequestMethod.GET)
	@ResponseBody
	public String userprofileimage(@RequestParam(required = false) String userphoto, HttpServletRequest req) {
		if (userphoto == null || userphoto.isEmpty()) {
			userphoto = (String) req.getSession().getAttribute("USERID");
		}
		if (userphoto != null) {
			BLRS_UserProfile_Entity user = loginServices.getUser(userphoto);
			if (user != null && user.getPhoto() != null && user.getPhoto().length > 0) {
				return Base64.getEncoder().encodeToString(user.getPhoto());
			}
		}
		return "Photo content is null";
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Other Menu Placeholders
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "Batchjobscheduler", method = { RequestMethod.GET, RequestMethod.POST })
	public String Batchjobscheduler(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_BatchJobScheduler";
	}

	@RequestMapping(value = "Batchjobalert", method = { RequestMethod.GET, RequestMethod.POST })
	public String Batchjobalert(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_BatchJobAlert";
	}

	@RequestMapping(value = "Parameters", method = { RequestMethod.GET, RequestMethod.POST })
	public String Parameters(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_Parameters";
	}

	@RequestMapping(value = "EmailandSMS", method = { RequestMethod.GET, RequestMethod.POST })
	public String EmailandSMS(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_Parameters";
	}

	@RequestMapping(value = "Reminder", method = { RequestMethod.GET, RequestMethod.POST })
	public String Reminder(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_ReminderParameter";
	}

	@RequestMapping(value = "Loanaccountprofile", method = { RequestMethod.GET, RequestMethod.POST })
	public String Loanaccountprofile(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_LoanAccountProfile";
	}

	@RequestMapping(value = "Remindergeneration", method = { RequestMethod.GET, RequestMethod.POST })
	public String Remindergeneration(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_ReminderGeneration";
	}

	@RequestMapping(value = "Dispatchdetails", method = { RequestMethod.GET, RequestMethod.POST })
	public String Dispatchdetails(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_Dispatchdetails";
	}

	@RequestMapping(value = "Batchjobexecution", method = { RequestMethod.GET, RequestMethod.POST })
	public String Batchjobexecution(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_Batchjobexecution";
	}

	@RequestMapping(value = "Loanaccountprofileinquiry", method = { RequestMethod.GET, RequestMethod.POST })
	public String Loanaccountprofileinquiry(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_LoanAccountProfileInquiries";
	}

	@RequestMapping(value = "Overduereports", method = { RequestMethod.GET, RequestMethod.POST })
	public String Overduereports(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_Overduereports";
	}

	@RequestMapping(value = "Reminderreports", method = { RequestMethod.GET, RequestMethod.POST })
	public String Reminderreports(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_Reminderreports";
	}

	@RequestMapping(value = "Guarantorreports", method = { RequestMethod.GET, RequestMethod.POST })
	public String Guarantorreports(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {
		md.addAttribute("formmode", formmode != null ? formmode : "list");
		return "BLRS_Guarantorreports";
	}

	@RequestMapping(value = "Useroperation", method = { RequestMethod.GET, RequestMethod.POST })
	public String Useroperation(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_Useroperation";
	}

	@RequestMapping(value = "Businessoperation", method = { RequestMethod.GET, RequestMethod.POST })
	public String Businessoperation(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_Businessoperation";
	}

}