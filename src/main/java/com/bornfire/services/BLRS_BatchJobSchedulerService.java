package com.bornfire.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
		BLRS_BatchJobScheduler_Entity j = batchJobRepo.getJob(jobId.trim());
		// verified-deleted jobs behave as "not found"
		if (j == null || "Y".equals(j.getDel_flg())) {
			return null;
		}
		fillSchedule(j);
		return j;
	}

	public BLRS_BatchJobScheduler_Entity getNewJob() {
		BLRS_BatchJobScheduler_Entity j = new BLRS_BatchJobScheduler_Entity();
		j.setJob_id(nextJobId());
		j.setJob_type("BUSINESS JOBS");
		j.setPeriodicity("DAILY");
		j.setDepartment("ALL");
		j.setStatus("ACTIVE");
		j.setEmail_flg("N");
		j.setSms_flg("N");
		Calendar c = Calendar.getInstance();
		j.setStart_date(c.getTime());
		c.set(2099, Calendar.DECEMBER, 31);
		j.setEnd_date(c.getTime());
		fillSchedule(j);
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
		if (isBlank(in.getDescription())) {
			return "Description is required";
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
			in.setStatus("ACTIVE");
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
		j.setNext_run_date(computeNextFor(null, in));
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
		if ("Y".equals(in.getEmail_flg()) && isBlank(in.getEmail_pwd()) && isBlank(ex.getEmail_pwd())) {
			return "Please enter Email Password";
		}

		// Manual : only these fields are modifiable
		ex.setPeriodicity(in.getPeriodicity());
		ex.setDepartment(in.getDepartment());
		ex.setStart_date(in.getStart_date());
		ex.setEnd_date(in.getEnd_date());
		ex.setStatus(normalizeStatus(in.getStatus()));
		ex.setNext_run_date(computeNextFor(ex.getLast_run_date(), in));
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
	/** Delete request by maker : status = Delete, record goes back to Unverified. Checker verify => DEL_FLG = Y */
	@Transactional
	public String deleteJob(String jobId, String loginUser) {
		BLRS_BatchJobScheduler_Entity ex = getJob(jobId);
		if (ex == null) {
			return "Job Id not found";
		}
		if ("Delete".equalsIgnoreCase(ex.getStatus()) && "N".equals(ex.getEntity_flg())) {
			return "Job Id: " + ex.getJob_id() + " is already waiting for delete verification.";
		}
		ex.setStatus("DELETE");
		ex.setEntity_flg("N");
		ex.setModify_user(loginUser);
		ex.setModify_time(new Date());
		batchJobRepo.save(ex);
		return "Job Id: " + ex.getJob_id() + " Delete request submitted Successfully. Verifier has to verify.";
	}

	@Transactional
	public String verifyJob(String jobId, String loginUser) {
		BLRS_BatchJobScheduler_Entity ex = getJob(jobId);
		if (ex == null) {
			return "Job Id not found";
		}
		if ("Y".equals(ex.getEntity_flg())) {
			return "Job Name: " + ex.getJob_name() + " is already verified.";
		}
		// Maker-Checker : the user who made the last change (modify / delete request, else entry) cannot verify
		String lastMaker = !isBlank(ex.getModify_user()) ? ex.getModify_user() : ex.getEntry_user();
		if (lastMaker != null && loginUser != null && lastMaker.trim().equalsIgnoreCase(loginUser.trim())) {
			return "Same user cannot verify";
		}
		ex.setEntity_flg("Y");
		ex.setAuth_user(loginUser);
		ex.setAuth_time(new Date());
		boolean deleted = "Delete".equalsIgnoreCase(ex.getStatus());
		if (deleted) {
			ex.setDel_flg("Y");
		}
		batchJobRepo.save(ex);
		return "Job Name: " + ex.getJob_name() + (deleted ? " Deleted Successfully." : " Verified Successfully.");
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

	private Date computeNextFor(Date last, BLRS_BatchJobScheduler_Entity in) {
		return computeNext(last, in.getStart_date(), in.getPeriodicity(), intOr(in.getDay(), 1),
				intOr(in.getHour(), 0), intOr(in.getHour1(), 0));
	}

	/**
	 * Next run = first slot (period / day / hh:mm) after the last run.
	 * First run of a new job = first slot on or after Start Date.
	 */
	private Date computeNext(Date last, Date start, String periodicity, int day, int hour, int minute) {
		boolean inclusive = (last == null);
		Date ref = inclusive ? start : last;
		if (ref == null) {
			ref = new Date();
		}
		Calendar c = Calendar.getInstance();
		c.setTime(ref);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		if ("WEEKLY".equalsIgnoreCase(periodicity)) {
			c.set(Calendar.DAY_OF_WEEK, (day % 7) + 1); // 1=Mon .. 7=Sun
			while (!slotOk(c, ref, inclusive)) {
				c.add(Calendar.DAY_OF_MONTH, 7);
			}
		} else if ("MONTHLY".equalsIgnoreCase(periodicity)) {
			c.set(Calendar.DAY_OF_MONTH, Math.min(day, c.getActualMaximum(Calendar.DAY_OF_MONTH)));
			while (!slotOk(c, ref, inclusive)) {
				c.set(Calendar.DAY_OF_MONTH, 1);
				c.add(Calendar.MONTH, 1);
				c.set(Calendar.DAY_OF_MONTH, Math.min(day, c.getActualMaximum(Calendar.DAY_OF_MONTH)));
			}
		} else {
			while (!slotOk(c, ref, inclusive)) {
				c.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		return c.getTime();
	}

	private boolean slotOk(Calendar c, Date ref, boolean inclusive) {
		return inclusive ? !c.getTime().before(ref) : c.getTime().after(ref);
	}

	/** Rebuilds the pop-up values (period / day / hour / minute) from PERIODICITY + NEXT_RUN_DATE. */
	private void fillSchedule(BLRS_BatchJobScheduler_Entity j) {
		String p = isBlank(j.getPeriodicity()) ? "DAILY" : j.getPeriodicity().trim().toUpperCase();
		j.setPeriod(p);
		int day = 1;
		int hour = 0;
		int minute = 0;
		Date n = j.getNext_run_date();
		if (n != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(n);
			hour = c.get(Calendar.HOUR_OF_DAY);
			minute = c.get(Calendar.MINUTE);
			if ("WEEKLY".equals(p)) {
				day = ((c.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1; // Mon=1 .. Sun=7
			} else if ("MONTHLY".equals(p)) {
				day = c.get(Calendar.DAY_OF_MONTH);
			}
		}
		j.setDay(day);
		j.setHour(hour);
		j.setHour1(minute);
	}

	private int intOr(Integer v, int def) {
		return v == null ? def : v.intValue();
	}

	/** DB (old data) keeps STATUS in upper case : ACTIVE / SUSPEND / DELETE */
	private String normalizeStatus(String s) {
		if ("Suspend".equalsIgnoreCase(s)) {
			return "SUSPEND";
		}
		if ("Delete".equalsIgnoreCase(s)) {
			return "DELETE";
		}
		return "ACTIVE";
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
	

	// ---------- Alert grid : jobs due today ----------
	public List<BLRS_BatchJobScheduler_Entity> getAlertList() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return batchJobRepo.getAlertList(c.getTime());
	}
 
	// ---------- Run button ----------
	public String runJob(String jobId, String loginUser) {
 
		BLRS_BatchJobScheduler_Entity job = batchJobRepo.findById(jobId).orElse(null);
		if (job == null) {
			return "Error : Job " + jobId + " not found.";
		}
		if (!"ACTIVE".equalsIgnoreCase(job.getStatus()) || !"Y".equals(job.getEntity_flg())) {
			return "Error : Job " + job.getJob_name() + " is not active or not verified.";
		}
 
		try {
			job.setJob_status("STARTED");
			batchJobRepo.save(job);
 
			executeJob(job); // <<< your existing execution (same as Operation > Batch Job Execution)
 
			job.setJob_status("COMPLETED");
			job.setLast_run_date(new Date());
			job.setNext_run_date(calcNextRun(job.getNext_run_date(), job.getPeriodicity()));
			job.setModify_user(loginUser);
			job.setModify_time(new Date());
			batchJobRepo.save(job);
 
			return "Job " + job.getJob_name() + " completed successfully.";
 
		} catch (Exception e) {
			e.printStackTrace();
			job.setJob_status("FAILED");
			batchJobRepo.save(job);
			return "Error : Job " + job.getJob_name() + " failed.";
		}
	}
 
	// ---------- put your real job logic here ----------
	private void executeJob(BLRS_BatchJobScheduler_Entity job) throws Exception {
		// e.g. if DATA JOBS -> extraction ; if BUSINESS JOBS -> reminder generation for job.getSchm_code()
		// If you already have a method for Batch Job Execution, just call it here.
	}
 
	// ---------- next run : daily +1 day, weekly +7 days, monthly +1 month (keeps the time) ----------
	// Replace with the same logic you used in saveJob() if you want exact day/hour rules.
	private Date calcNextRun(Date current, String periodicity) {
		Calendar c = Calendar.getInstance();
		c.setTime(current != null ? current : new Date());
		String p = periodicity == null ? "" : periodicity.toUpperCase();
 
		Date now = new Date();
		do {
			if (p.contains("WEEK")) {
				c.add(Calendar.DAY_OF_MONTH, 7);
			} else if (p.contains("MONTH")) {
				c.add(Calendar.MONTH, 1);
			} else {
				c.add(Calendar.DAY_OF_MONTH, 1);
			}
		} while (!c.getTime().after(now)); // never leave next run in the past, or it shows in the alert again
 
		return c.getTime();
	}
}