package com.bornfire.services;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_BatchJobExecution_Entity;
import com.bornfire.entities.BLRS_BatchJobExecution_Repo;
import com.bornfire.entities.BLRS_BatchJobScheduler_Entity;
import com.bornfire.entities.BLRS_BatchJobScheduler_Repo;
import com.bornfire.entities.BLRS_BatchJobScheme_Entity;
import com.bornfire.entities.BLRS_BatchJobScheme_Repo;
import com.bornfire.entities.BLRS_EmailSmsParam_Entity;
import com.bornfire.entities.BLRS_ReminderGenerator_Entity;
import com.bornfire.entities.BLRS_ReminderParam_Entity;

@Service
public class BLRS_BatchJobSchedulerService {

	private static final org.slf4j.Logger log =
			org.slf4j.LoggerFactory.getLogger(BLRS_BatchJobSchedulerService.class);

	@Autowired private BLRS_NotificationService       notificationService;
	@Autowired private com.bornfire.entities.BLRS_EmailSmsParam_Repo emailSmsRepo;
	@Autowired private BLRS_ParameterService           parameterService;
	@Autowired private BLRS_ReminderGeneratorService   reminderGenService;
	@Autowired private BLRS_BatchJobScheduler_Repo     batchJobRepo;
	@Autowired private BLRS_BatchJobExecution_Repo     executionRepo;
	@Autowired private BLRS_BatchJobScheme_Repo        schemeChildRepo;
	@Autowired private FinacleExtractionService        finacleExtractionService;

	@Value("${output.exportpath:D:\\\\}")
	private String exportPath;

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
		if (j == null) {
			j = batchJobRepo.findById(jobId.trim()).orElse(null);
		}
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
				target.setEmail_pwd(com.bornfire.config.AES.encryptSafe(in.getEmail_pwd()));
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

	// ---------- Run button (manual trigger) ----------
	@Transactional
	public String runJob(String jobId, String loginUser) {

		BLRS_BatchJobScheduler_Entity job = batchJobRepo.findById(jobId).orElse(null);
		if (job == null) {
			return "Error: Job " + jobId + " not found.";
		}
		if (!"ACTIVE".equalsIgnoreCase(job.getStatus()) || !"Y".equals(job.getEntity_flg())) {
			return "Error: Job '" + job.getJob_name() + "' is not active or not verified.";
		}

		// --- Rule 6: Business jobs require same-day FIN001 COMPLETED ---
		if ("BUSINESS JOBS".equalsIgnoreCase(job.getJob_type())) {
			Date today = truncateToDate(new Date());
			boolean fin001Done = executionRepo.isJobCompletedOnDate("FIN001", today);
			if (!fin001Done) {
				return "Error: Business job '" + job.getJob_name()
						+ "' cannot run until same-day FIN001 extraction is COMPLETED.";
			}
		}

		// --- Sync scheme child table ---
		syncSchemeChildTable(job);

		// --- Create BatchJobExecution record (STARTED) ---
		Date startTime = new Date();
		String executionId = "EXE-" + job.getJob_id() + "-" +
				new SimpleDateFormat("yyyyMMdd-HHmmss").format(startTime) + "-" +
				UUID.randomUUID().toString().substring(0, 6).toUpperCase();

		BLRS_BatchJobExecution_Entity exec = new BLRS_BatchJobExecution_Entity();
		exec.setExecutionId(executionId);
		exec.setJobId(job.getJob_id());
		exec.setJobName(job.getJob_name());
		exec.setRunDate(truncateToDate(startTime));
		exec.setStartTime(startTime);
		exec.setStatus("STARTED");
		exec.setRunBy(loginUser != null ? loginUser : "SYSTEM");
		exec.setRemarks("Job execution started");
		executionRepo.save(exec);

		// Mark scheduler entity as STARTED
		job.setJob_status("STARTED");
		batchJobRepo.save(job);

		int totalRecords = 0;
		int successCount = 0;
		int failureCount = 0;
		String statusFilePath = null;

		try {
			// --- Execute the actual job logic ---
			int[] counts = executeJob(job, loginUser, exec.getRunDate());
			totalRecords = counts[0];
			successCount = counts[1];
			failureCount = counts[2];

			// --- Generate Excel status file ---
			statusFilePath = generateBusinessStatusExcel(job.getJob_id(), job.getJob_name(),
					exec.getRunDate(), startTime, new Date(), "COMPLETED",
					totalRecords, successCount, failureCount, loginUser);

			// --- Complete execution record ---
			exec.setEndTime(new Date());
			exec.setStatus("COMPLETED");
			exec.setTotalRecords(totalRecords);
			exec.setSuccessCount(successCount);
			exec.setFailureCount(failureCount);
			exec.setStatusFilePath(statusFilePath);
			exec.setRemarks("Completed: processed=" + totalRecords + ", success=" + successCount + ", failed=" + failureCount);
			executionRepo.save(exec);

			// --- Update scheduler entity ---
			job.setJob_status("COMPLETED");
			job.setLast_run_date(new Date());
			job.setNext_run_date(calcNextRun(job.getNext_run_date(), job.getPeriodicity()));
			job.setModify_user(loginUser);
			job.setModify_time(new Date());
			batchJobRepo.save(job);

			log.info("Job {} COMPLETED: total={}, success={}, fail={}",
					job.getJob_id(), totalRecords, successCount, failureCount);
			return "Job '" + job.getJob_name() + "' completed successfully."
					+ (statusFilePath != null ? " Status file: " + statusFilePath : "");

		} catch (Exception e) {
			log.error("Job {} FAILED", job.getJob_id(), e);

			// --- Failed execution record ---
			try {
				exec.setEndTime(new Date());
				exec.setStatus("FAILED");
				exec.setTotalRecords(totalRecords);
				exec.setSuccessCount(successCount);
				exec.setFailureCount(failureCount);
				exec.setRemarks("FAILED: " + e.getMessage());
				executionRepo.save(exec);
			} catch (Exception ex2) {
				log.error("Could not update execution record on failure", ex2);
			}

			job.setJob_status("FAILED");
			batchJobRepo.save(job);
			return "Error: Job '" + job.getJob_name() + "' failed — " + e.getMessage();
		}
	}

	/**
	 * Executes the actual job logic.
	 * DATA JOBS  -> delegates to FinacleExtractionService (FIN001 / DRS002).
	 * BUSINESS JOBS -> triggers reminder generation + notification dispatch.
	 *
	 * @return int[3] { totalRecords, successCount, failureCount }
	 */
	private int[] executeJob(BLRS_BatchJobScheduler_Entity job, String loginUser, Date runDate) throws Exception {

		if ("DATA JOBS".equalsIgnoreCase(job.getJob_type())) {
			// --- FIN001 or DRS002 ---
			FinacleExtractionService.ExtractionResult result;
			if ("DRS002".equals(job.getJob_id())) {
				result = finacleExtractionService.executeDRS002(runDate, loginUser);
			} else {
				// Default DATA JOB (FIN001 or any custom data job)
				result = finacleExtractionService.executeFIN001(runDate, loginUser);
			}
			if (!result.isSuccess()) {
				throw new Exception(result.getMessage());
			}
			return new int[]{ result.getTotalRecords(), result.getSuccessCount(), result.getFailureCount() };

		} else {
			// --- BUSINESS JOBS (Reminder generation + Notification dispatch) ---
			BLRS_EmailSmsParam_Entity emailSms = emailSmsRepo.findById("EMAILSMS").orElse(null);

			String remNo = job.getReminder_type();
			Map<String, String> opt = buildReminderOptions(remNo);

			List<BLRS_ReminderGenerator_Entity> rows = reminderGenService.getReminderList();

			// Filter by scheme code if not ALL
			if (rows != null && !rows.isEmpty() && !isBlank(job.getSchm_code())
					&& !"ALL".equalsIgnoreCase(job.getSchm_code().trim())) {
				String[] schemes = job.getSchm_code().split("[,\\s]+");
				Set<String> schemeSet = new java.util.HashSet<>(Arrays.asList(schemes));
				List<BLRS_ReminderGenerator_Entity> filtered = new ArrayList<>();
				for (BLRS_ReminderGenerator_Entity r : rows) {
					if (r.getSchem_code() != null && schemeSet.contains(r.getSchem_code().trim())) {
						filtered.add(r);
					}
				}
				rows = filtered;
			}

			int total = rows != null ? rows.size() : 0;

			// Dispatch email / SMS notifications
			if (emailSms != null && opt != null && rows != null && !rows.isEmpty()) {
				notificationService.dispatchReminders(emailSms, opt, rows,
						"Y".equalsIgnoreCase(job.getEmail_flg()),
						"Y".equalsIgnoreCase(job.getSms_flg()),
						loginUser);
			}

			return new int[]{ total, total, 0 };
		}
	}

	/** Build reminder options map from reminder type string (REM1 / REM2 / REM3). */
	private Map<String, String> buildReminderOptions(String remNo) {
		if (isBlank(remNo)) return null;
		String remDigits = remNo.replaceAll("[^0-9]", "");
		for (com.bornfire.entities.BLRS_ReminderParam_Entity rp : parameterService.getReminders()) {
			if (rp.getReminder_no() == null) continue;
			String rpDigits = rp.getReminder_no().replaceAll("[^0-9]", "");
			if (rp.getReminder_no().equalsIgnoreCase(remNo) || (!remDigits.isEmpty() && remDigits.equals(rpDigits))) {
				Map<String, String> m = new java.util.LinkedHashMap<>();
				m.put("no",        rp.getReminder_no());
				m.put("name",      "Reminder-" + (rpDigits.isEmpty() ? "1" : rpDigits));
				m.put("download",  rp.getDownload_flg());
				m.put("signature", rp.getSignature_flg());
				m.put("email",     rp.getEmail_flg());
				m.put("sms",       rp.getSms_flg());
				return m;
			}
		}
		return null;
	}

	/** Synchronise BLRS_BATCH_JOB_SCHEME_TAB from the comma-separated schm_code column. */
	private void syncSchemeChildTable(BLRS_BatchJobScheduler_Entity job) {
		try {
			if (isBlank(job.getJob_id()) || isBlank(job.getSchm_code())) return;
			schemeChildRepo.deleteByJobId(job.getJob_id());
			String[] codes = job.getSchm_code().split("[,\\s]+");
			for (String code : codes) {
				if (!code.trim().isEmpty()) {
					schemeChildRepo.save(
							new BLRS_BatchJobScheme_Entity(job.getJob_id(), code.trim().toUpperCase()));
				}
			}
		} catch (Exception e) {
			log.warn("Could not sync scheme child table for job {}: {}", job.getJob_id(), e.getMessage());
		}
	}

	/** Generates a standard Apache POI status Excel file for business jobs. */
	public String generateBusinessStatusExcel(String jobId, String jobName, Date runDate,
			Date startTime, Date endTime, String status,
			int total, int success, int failure, String user) {
		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(runDate != null ? runDate : new Date());
		String fileName = jobId + "(" + dateStr + ")Status.xls";
		File dir = new File(exportPath != null && !exportPath.trim().isEmpty()
				? exportPath : System.getProperty("java.io.tmpdir"));
		if (!dir.exists()) dir.mkdirs();
		File targetFile = new File(dir, fileName);

		try (Workbook wb = new HSSFWorkbook()) {
			Sheet sheet = wb.createSheet("Execution Status");

			Font titleFont = wb.createFont(); titleFont.setBold(true); titleFont.setFontHeightInPoints((short) 12);
			CellStyle titleStyle = wb.createCellStyle(); titleStyle.setFont(titleFont);

			Font headerFont = wb.createFont(); headerFont.setBold(true);
			CellStyle headerStyle = wb.createCellStyle(); headerStyle.setFont(headerFont);

			Row r0 = sheet.createRow(0);
			Cell c0 = r0.createCell(0);
			c0.setCellValue("Bank Loan Recovery System (BLRS) – Batch Job Execution Report");
			c0.setCellStyle(titleStyle);

			sheet.createRow(1).createCell(0)
					.setCellValue("Report Generated: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));

			String[] headers = { "Job ID", "Job Name", "Run Date", "Start Time", "End Time",
					"Status", "Total Records", "Success Count", "Failure Count", "Executed By" };
			Row rh = sheet.createRow(3);
			for (int i = 0; i < headers.length; i++) {
				Cell ch = rh.createCell(i); ch.setCellValue(headers[i]); ch.setCellStyle(headerStyle);
			}

			SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
			Row rd = sheet.createRow(4);
			rd.createCell(0).setCellValue(jobId);
			rd.createCell(1).setCellValue(jobName);
			rd.createCell(2).setCellValue(dateStr);
			rd.createCell(3).setCellValue(startTime != null ? tf.format(startTime) : "");
			rd.createCell(4).setCellValue(endTime   != null ? tf.format(endTime)   : "");
			rd.createCell(5).setCellValue(status);
			rd.createCell(6).setCellValue(total);
			rd.createCell(7).setCellValue(success);
			rd.createCell(8).setCellValue(failure);
			rd.createCell(9).setCellValue(user != null ? user : "");

			for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

			try (FileOutputStream fos = new FileOutputStream(targetFile)) {
				wb.write(fos);
			}
			log.info("Status Excel written: {}", targetFile.getAbsolutePath());
			return targetFile.getAbsolutePath();
		} catch (Exception e) {
			log.error("Failed to generate status Excel for job {}", jobId, e);
			return null;
		}
	}

	/** Get execution history for a specific job ID. */
	public List<BLRS_BatchJobExecution_Entity> getExecutionHistory(String jobId) {
		if (isBlank(jobId)) return java.util.Collections.emptyList();
		return executionRepo.findByJobIdOrderByStartTimeDesc(jobId);
	}

	/** Get all recent executions (for the Batch Job Execution inquiry screen). */
	public List<BLRS_BatchJobExecution_Entity> getAllRecentExecutions() {
		return executionRepo.findAllRecentExecutions();
	}

	/** Get the stored status file path for a given execution. */
	public String getStatusFilePath(String executionId) {
		if (isBlank(executionId)) return null;
		BLRS_BatchJobExecution_Entity exec = executionRepo.findById(executionId).orElse(null);
		return exec != null ? exec.getStatusFilePath() : null;
	}

	// ---------- next run calculation ----------
	private Date calcNextRun(Date current, String periodicity) {
		Calendar c = Calendar.getInstance();
		c.setTime(current != null ? current : new Date());
		String p = periodicity == null ? "" : periodicity.toUpperCase();
		Date now = new Date();
		do {
			if (p.contains("WEEK"))       c.add(Calendar.DAY_OF_MONTH, 7);
			else if (p.contains("MONTH")) c.add(Calendar.MONTH, 1);
			else                          c.add(Calendar.DAY_OF_MONTH, 1);
		} while (!c.getTime().after(now));
		return c.getTime();
	}

	/** Truncate a Date to midnight (date only, no time component). */
	private Date truncateToDate(Date d) {
		if (d == null) return null;
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
}