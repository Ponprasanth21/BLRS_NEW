package com.bornfire.controller;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.bornfire.entities.BLRS_UserProfile_Entity;
import com.bornfire.entities.BLRS_UserProfile_Repo;
import com.bornfire.services.LoginServices;
import com.bornfire.services.ListofDataService;

@Controller
public class NavigationController {

	private static final Logger logger = LoggerFactory.getLogger(NavigationController.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	LoginServices LoginServices;

	@Autowired
	BLRS_UserProfile_Repo UserProfileRep;
	
	@Autowired
	ListofDataService listofdataService;

	// ---------------------------------------------------------------------------------------------------------------
	// ---------------------------Login
	// Reset--------------------------------------------------------------------------

	@RequestMapping(value = "changePasswordLogin", method = { RequestMethod.GET, RequestMethod.POST })
	public String changePasswordLogin(@RequestParam(required = false) String formmode, Model md, HttpServletRequest req)
			throws ParseException {
		System.out.println("reset password");
		return "BLRS_ChangePasswordLogin";
	}

	@RequestMapping(value = "resetPassword", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String resetPassword(@RequestParam(required = false) String formmode, Model md, HttpServletRequest req,
			@RequestParam(required = false) String userid) throws ParseException {
		System.out.println("userid" + userid);
		Optional<BLRS_UserProfile_Entity> singlerecords = UserProfileRep.findById(userid);
		BLRS_UserProfile_Entity singlerecord = singlerecords.get();
		if (singlerecord == null) {
			return "User not found!";
		}
		singlerecord.setPassword("9yXzwvfDl/wv+IZLxAHX6A==");
		UserProfileRep.save(singlerecord);

		return "Successully Password Reset";
	}

	@RequestMapping(value = "rest_password", method = RequestMethod.POST)
	@ResponseBody
	public String rest_password(@RequestParam("old_password") String old_password,
			@RequestParam("new_password") String new_password, @RequestParam("user_id") String userid, Model md,
			HttpServletRequest rq) {

		System.out.println("Password reset attempt by user: " + userid);

		String msg = LoginServices.changePassword(old_password, new_password, userid);
		md.addAttribute("message", "success");
		return msg;
	}

	// Administration Navigations
	// ---------------------------------------------------------------------------------------------------------------------------

	@GetMapping("/Dashboard")
	public String getMethodName(Model md) {
		return "BLRS_Dashboard";
	}

	@RequestMapping(value = "Userprofile", method = { RequestMethod.GET, RequestMethod.POST })
	public String Userprofile(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equalsIgnoreCase("list")) {

			md.addAttribute("formmode", "list");

			List<BLRS_UserProfile_Entity> userProfiles = listofdataService.getUsersList();

			md.addAttribute("userProfiles", userProfiles);
		}

		return "BLRS_UserProfile";
	}

	@RequestMapping(value = "Accesscontrol", method = { RequestMethod.GET, RequestMethod.POST })
	public String Accesscontrol(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_Accesscontrol";
	}

	@RequestMapping(value = "Batchjobscheduler", method = { RequestMethod.GET, RequestMethod.POST })
	public String Batchjobscheduler(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_BatchJobScheduler";
	}

	@RequestMapping(value = "Batchjobalert", method = { RequestMethod.GET, RequestMethod.POST })
	public String Batchjobalert(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_BatchJobAlert";
	}

	@RequestMapping(value = "Parameters", method = { RequestMethod.GET, RequestMethod.POST })
	public String Parameters(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_Parameters";
	}

	@RequestMapping(value = "Loanaccountprofile", method = { RequestMethod.GET, RequestMethod.POST })
	public String Loanaccountprofile(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_LoanAccountProfile";
	}

	@RequestMapping(value = "Remindergeneration", method = { RequestMethod.GET, RequestMethod.POST })
	public String Remindergeneration(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_ReminderGeneration";
	}

	@RequestMapping(value = "Dispatchdetails", method = { RequestMethod.GET, RequestMethod.POST })
	public String Dispatchdetails(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_Dispatchdetails";
	}

	@RequestMapping(value = "Batchjobexecution", method = { RequestMethod.GET, RequestMethod.POST })
	public String Batchjobexecution(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_Batchjobexecution";
	}

	@RequestMapping(value = "Loanaccountprofileinquiry", method = { RequestMethod.GET, RequestMethod.POST })
	public String Loanaccountprofileinquiry(@RequestParam(required = false) String formmode, Model md,
			HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_LoanAccountProfileInquiries";
	}

	@RequestMapping(value = "Overduereports", method = { RequestMethod.GET, RequestMethod.POST })
	public String Overduereports(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_Overduereports";
	}

	@RequestMapping(value = "Reminderreports", method = { RequestMethod.GET, RequestMethod.POST })
	public String Reminderreports(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		return "BLRS_Reminderreports";
	}

	@RequestMapping(value = "Guarantorreports", method = { RequestMethod.GET, RequestMethod.POST })
	public String Guarantorreports(@RequestParam(required = false) String formmode, Model md, HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

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