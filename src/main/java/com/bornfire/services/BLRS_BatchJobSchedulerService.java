package com.bornfire.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_BatchJobScheduler_Entity;
import com.bornfire.entities.BLRS_BatchJobScheduler_Repo;

@Service
public class BLRS_BatchJobSchedulerService {

	@Autowired
	private BLRS_BatchJobScheduler_Repo batchJobRepo;

	private static final String[] BASE_SCHEMES = { "ALL", "CLN2", "CLN4", "CPL1", "DPEMI", "ELN1", "FINLI", "HOULN",
			"LNBPA", "LNEST", "MQL1", "PLBPA", "PLN1", "PPL1", "SMEAG", "SMELG", "SSL1", "TLEMI" };

	private static final String[] BASE_DEPTS = { "ALL", "EDUC", "SAVN" };

	// ------------------------------------------------------------------
	// Read
	// ------------------------------------------------------------------
	public List<BLRS_BatchJobScheduler_Entity> getJobList() {
		return batchJobRepo.getJobList();
	}

	public BLRS_BatchJobScheduler_Entity getJob(String jobId) {
		if (jobId == null || jobId.trim().isEmpty()) {
			return null;
		}
		return batchJobRepo.getJob(jobId.trim());
	}

	public BLRS_BatchJobScheduler_Entity getNewJob() {
		BLRS_BatchJobScheduler_Entity j = new BLRS_BatchJobScheduler_Entity();
		j.setJob_id(nextJobId());
		j.setJob_type("BUSINESS JOBS");
		j.setPeriodicity("DAILY");
		j.setDepartment("ALL");
		j.setStatus("Active");
		j.setEmail_flg("N");
		j.setSms_flg("N");
		Calendar c = Calendar.getInstance();
		j.setStart_date(c.getTime());
		c.set(2099, Calendar.DECEMBER, 31);
		j.setEnd_date(c.getTime());
		return j;
	}

	public List<String> getSchemeCodes() {
		Set<String> s = new TreeSet<String>(Arrays.asList(BASE_SCHEMES));
		for (String v : batchJobRepo.getSchemeValues()) {
			for (String t : v.split(",")) {
				if (!t.trim().isEmpty()) {
					s.add(t.trim().toUpperCase());
				}
			}
		}
		return new ArrayList<String>(s);
	}

	public List<String> getDepartments() {
		Set<String> s = new LinkedHashSet<String>(Arrays.asList(BASE_DEPTS));
		for (String d : batchJobRepo.getDepartmentValues()) {
			if (!d.trim().isEmpty()) {
				s.add(d.trim().toUpperCase());
			}
		}
		return new ArrayList<String>(s);
	}

	public List<String> getReminderTypes() {
		return Arrays.asList("REM1", "REM2", "REM3");
	}

	public List<String> getPeriodicities() {
		return Arrays.asList("DAILY", "WEEKLY", "MONTHLY");
	}

	// ------------------------------------------------------------------
	// Add / Modify
	// ------------------------------------------------------------------
	@Transactional
	public String saveJob(BLRS_BatchJobScheduler_Entity in, String formmode, String loginUser) {
		if ("add".equalsIgnoreCase(formmode)) {
			return addJob(in, loginUser);
		} else if ("edit".equalsIgnoreCase(formmode)) {
			return modifyJob(in, loginUser);
		}
		return "Invalid form mode";
	}

	private String addJob(BLRS_BatchJobScheduler_Entity in, String loginUser) {
		if (isBlank(in.getJob_name())) {
			return "Job Name is required";
		}
		if (isBlank(in.getJob_type())) {
			in.setJob_type("BUSINESS JOBS");
		}
		if ("BUSINESS JOBS".equalsIgnoreCase(in.getJob_type())) {
			if (isBlank(in.getSchm_code())) {
				return "Scheme Code is required";
			}
			if (isBlank(in.getReminder_type())) {
				return "Reminder Type is required";
			}
		} else {
			in.setSchm_code("ALL");
			in.setReminder_type(null);
		}
		if (batchJobRepo.countActiveByName(in.getJob_name().trim()) > 0) {
			return "Job Name already exists";
		}
		if (isBlank(in.getStatus())) {
			in.setStatus("Active");
		}
		if ("Delete".equalsIgnoreCase(in.getStatus())) {
			return "Delete status is not allowed while adding a job";
		}
		String err = validateCommon(in);
		if (err != null) {
			return err;
		}

		BLRS_BatchJobScheduler_Entity j = new BLRS_BatchJobScheduler_Entity();
		j.setJob_id(nextJobId());
		j.setJob_name(in.getJob_name().trim());
		j.setDescription(in.getDescription());
		j.setJob_type(in.getJob_type().toUpperCase());
		j.setSchm_code(in.getSchm_code().trim().toUpperCase());
		j.setReminder_type(in.getReminder_type());
		j.setDepartment(in.getDepartment());
		j.setPeriodicity(in.getPeriodicity());
		j.setStart_date(in.getStart_date());
		j.setEnd_date(in.getEnd_date());
		j.setLast_run_date(null);
		j.setNext_run_date(computeNext(null, in.getStart_date(), in.getPeriodicity()));
		j.setStatus(normalizeStatus(in.getStatus()));
		applyNotify(j, in, null);
		j.setEntity_flg("N");
		j.setDel_flg("N");
		j.setEntry_user(loginUser);
		j.setEntry_time(new Date());
		batchJobRepo.save(j);
		return "Job Id: " + j.getJob_id() + " added Successfully.";
	}

	private String modifyJob(BLRS_BatchJobScheduler_Entity in, String loginUser) {
		BLRS_BatchJobScheduler_Entity ex = getJob(in.getJob_id());
		if (ex == null) {
			return "Job Id not found";
		}
		if (isBlank(in.getStatus())) {
			in.setStatus(ex.getStatus());
		}
		// validate BEFORE touching the managed entity
		String err = validateCommon(in);
		if (err != null) {
			return err;
		}

		// Manual : only these fields are modifiable
		ex.setPeriodicity(in.getPeriodicity());
		ex.setDepartment(in.getDepartment());
		ex.setStart_date(in.getStart_date());
		ex.setEnd_date(in.getEnd_date());
		ex.setStatus(normalizeStatus(in.getStatus()));
		ex.setNext_run_date(computeNext(ex.getLast_run_date(), in.getStart_date(), in.getPeriodicity()));
		applyNotify(ex, in, ex);
		ex.setEntity_flg("N");
		ex.setModify_user(loginUser);
		ex.setModify_time(new Date());
		batchJobRepo.save(ex);
		return "Job Id: " + ex.getJob_id() + " Modified Successfully.";
	}

	// ------------------------------------------------------------------
	// Verify (checker)
	// ------------------------------------------------------------------
	@Transactional
	public String verifyJob(String jobId, String loginUser) {
		BLRS_BatchJobScheduler_Entity ex = getJob(jobId);
		if (ex == null) {
			return "Job Id not found";
		}
		if ("Y".equals(ex.getEntity_flg())) {
			return "Job Name: " + ex.getJob_name() + " is already verified.";
		}
		String lastMaker = !isBlank(ex.getModify_user()) ? ex.getModify_user() : ex.getEntry_user();
		if (lastMaker != null && lastMaker.equalsIgnoreCase(loginUser)) {
			return "Same user cannot verify";
		}
		ex.setEntity_flg("Y");
		ex.setAuth_user(loginUser);
		ex.setAuth_time(new Date());
		if ("Delete".equalsIgnoreCase(ex.getStatus())) {
			ex.setDel_flg("Y");
		}
		batchJobRepo.save(ex);
		return "Job Name: " + ex.getJob_name() + " Verified Successfully.";
	}

	// ------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------
	private String nextJobId() {
		Number n = batchJobRepo.getNextJobSeq();
		int seq = (n == null) ? 0 : n.intValue();
		return "BJ" + String.format("%03d", seq);
	}

	private String validateCommon(BLRS_BatchJobScheduler_Entity in) {
		if (isBlank(in.getPeriodicity())) {
			return "Periodicity is required";
		}
		if (isBlank(in.getDepartment())) {
			return "Department is required";
		}
		if (in.getStart_date() == null || in.getEnd_date() == null) {
			return "Start Date and End Date are required";
		}
		if (in.getEnd_date().before(in.getStart_date())) {
			return "End Date cannot be before Start Date";
		}
		if ("Y".equals(in.getEmail_flg())) {
			if (isBlank(in.getEmail_id()) || in.getEmail_id().indexOf('@') < 0) {
				return "Please enter a valid Email Id";
			}
		}
		return null;
	}

	/** Email details are kept only when Email = Y. Blank password on modify keeps the old one. */
	private void applyNotify(BLRS_BatchJobScheduler_Entity target, BLRS_BatchJobScheduler_Entity in,
			BLRS_BatchJobScheduler_Entity old) {
		target.setSms_flg("Y".equals(in.getSms_flg()) ? "Y" : "N");
		if ("Y".equals(in.getEmail_flg())) {
			target.setEmail_flg("Y");
			target.setEmail_id(in.getEmail_id());
			if (!isBlank(in.getEmail_pwd()) || old == null) {
				target.setEmail_pwd(in.getEmail_pwd());
			}
			target.setEmail_header(in.getEmail_header());
			target.setEmail_footer(in.getEmail_footer());
		} else {
			target.setEmail_flg("N");
			target.setEmail_id(null);
			target.setEmail_pwd(null);
			target.setEmail_header(null);
			target.setEmail_footer(null);
		}
	}

	private Date computeNext(Date last, Date start, String periodicity) {
		if (last == null) {
			return start;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(last);
		if ("WEEKLY".equalsIgnoreCase(periodicity)) {
			c.add(Calendar.DAY_OF_MONTH, 7);
		} else if ("MONTHLY".equalsIgnoreCase(periodicity)) {
			c.add(Calendar.MONTH, 1);
		} else {
			c.add(Calendar.DAY_OF_MONTH, 1);
		}
		Date n = c.getTime();
		if (start != null && n.before(start)) {
			n = start;
		}
		return n;
	}

	private String normalizeStatus(String s) {
		if ("Suspend".equalsIgnoreCase(s)) {
			return "Suspend";
		}
		if ("Delete".equalsIgnoreCase(s)) {
			return "Delete";
		}
		return "Active";
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}