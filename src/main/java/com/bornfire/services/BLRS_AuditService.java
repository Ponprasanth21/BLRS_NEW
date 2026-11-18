package com.bornfire.services;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.config.SequenceGenerator;
import com.bornfire.entities.BLRS_AuditTable;
import com.bornfire.entities.BLRS_AuditTable_Rep;

/**
 * Common audit writer used by Access Role, Batch Job Scheduler (and any other
 * admin screen). Everything is saved in BLRS_AUDIT_TABLE and is listed in
 * "User Activity Audits".
 */
@Service
public class BLRS_AuditService {

	private static final Logger logger = LoggerFactory.getLogger(BLRS_AuditService.class);

	private static final int MAX_LEN = 250;

	@Autowired
	private BLRS_AuditTable_Rep auditRepo;

	@Autowired
	private SequenceGenerator sequence;

	// =====================================================================
	// 1. Simple audit row (ADD / VERIFY / DELETE ...) - no field details
	// =====================================================================
	@Transactional
	public void saveRecord(String module, String tableName, String action, String recordId, String recordName,
			String loginUser, String authUser, String remarks) {
		writeRow(module, tableName, action, recordId, recordName, loginUser, authUser, remarks, "-", null, null);
	}

	// =====================================================================
	// 2. Snapshot of an entity : fieldName -> text value
	// Take it BEFORE save() (managed entities are overwritten by save()).
	// =====================================================================
	public Map<String, String> snapshot(Object entity, Set<String> skipFields) {
		Map<String, String> map = new LinkedHashMap<>();
		if (entity == null) {
			return map;
		}
		for (Class<?> c = entity.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
			for (Field f : c.getDeclaredFields()) {
				String n = f.getName();
				if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic() || n.startsWith("$")
						|| (skipFields != null && skipFields.contains(n)) || f.getType() == byte[].class) {
					continue;
				}
				try {
					f.setAccessible(true);
					map.put(n, text(f.get(entity)));
				} catch (Exception e) {
					logger.error("Audit snapshot failed for field " + n, e);
				}
			}
		}
		return map;
	}

	// =====================================================================
	// 3. Compare two snapshots and write one audit row per changed field
	// labels : optional display names (fieldName -> label)
	// listFields : comma separated fields (e.g. menulist) shown as Added / Removed
	// =====================================================================
	@Transactional
	public void saveChanges(String module, String tableName, String action, String recordId, String recordName,
			String loginUser, String remarks, Map<String, String> before, Map<String, String> after,
			Map<String, String> labels, Set<String> listFields) {

		int written = 0;
		for (Map.Entry<String, String> e : after.entrySet()) {
			String key = e.getKey();
			String o = before.get(key) == null ? "" : before.get(key);
			String n = e.getValue() == null ? "" : e.getValue();
			if (o.equals(n)) {
				continue;
			}
			String label = (labels != null && labels.containsKey(key)) ? labels.get(key) : label(key);

			if (listFields != null && listFields.contains(key)) {
				Set<String> oldSet = split(o);
				Set<String> newSet = split(n);
				Set<String> removed = new LinkedHashSet<>(oldSet);
				removed.removeAll(newSet);
				Set<String> added = new LinkedHashSet<>(newSet);
				added.removeAll(oldSet);
				if (!added.isEmpty()) {
					writeRow(module, tableName, action, recordId, recordName, loginUser, null, remarks,
							label + " (Added)", "-", String.join(", ", added));
					written++;
				}
				if (!removed.isEmpty()) {
					writeRow(module, tableName, action, recordId, recordName, loginUser, null, remarks,
							label + " (Removed)", String.join(", ", removed), "-");
					written++;
				}
				if (added.isEmpty() && removed.isEmpty()) {
					// only the order changed - nothing to show
				}
			} else {
				writeRow(module, tableName, action, recordId, recordName, loginUser, null, remarks, label,
						o.isEmpty() ? "-" : o, n.isEmpty() ? "-" : n);
				written++;
			}
		}
		if (written == 0) {
			// saved without any field change - still keep the activity
			writeRow(module, tableName, action, recordId, recordName, loginUser, null, remarks, "-", null, null);
		}
	}

	// =====================================================================
	// internals
	// =====================================================================
	private void writeRow(String module, String tableName, String action, String recordId, String recordName,
			String loginUser, String authUser, String remarks, String fieldName, String oldValue, String newValue) {
		try {
			Date now = new Date();
			BLRS_AuditTable audit = new BLRS_AuditTable();
			audit.setAudit_ref_no(sequence.generateRequestUUId());
			audit.setAudit_date(now);
			audit.setEntry_time(now);
			audit.setEntry_user(loginUser);
			audit.setAuth_user(authUser);
			audit.setAuth_time(authUser != null ? now : null);
			audit.setAudit_table(tableName);
			audit.setAudit_screen(module + " - " + action);
			audit.setFunc_code(action);
			audit.setEvent_id(recordId);
			audit.setEvent_name(recordName);
			audit.setField_name(cut(fieldName));
			audit.setOld_value(cut(oldValue));
			audit.setNew_value(cut(newValue));
			audit.setModi_details(remarks);
			audit.setRemarks(remarks);
			auditRepo.save(audit);
		} catch (Exception e) {
			logger.error("Audit write failed for " + tableName + " / " + recordId, e);
		}
	}

	private String text(Object v) {
		if (v == null) {
			return "";
		}
		if (v instanceof Date) {
			return new SimpleDateFormat("dd-MM-yyyy").format((Date) v);
		}
		if (v instanceof BigDecimal) {
			return ((BigDecimal) v).stripTrailingZeros().toPlainString();
		}
		return v.toString().trim();
	}

	/** "role_desc" -> "Role Desc" */
	private String label(String fieldName) {
		StringBuilder sb = new StringBuilder();
		for (String part : fieldName.split("_")) {
			if (part.isEmpty()) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return sb.toString();
	}

	private Set<String> split(String csv) {
		Set<String> s = new LinkedHashSet<>();
		if (csv != null) {
			for (String t : csv.split(",")) {
				if (!t.trim().isEmpty()) {
					s.add(t.trim());
				}
			}
		}
		return s;
	}

	private String cut(String v) {
		if (v == null) {
			return null;
		}
		return v.length() > MAX_LEN ? v.substring(0, MAX_LEN - 3) + "..." : v;
	}

	// =====================================================================
	// your earlier methods - unchanged
	// =====================================================================
	@Transactional
	public void saveAdminAudit(String module, String tableName, String action, String recordId, String recordName,
			String loginUser, String remarks) {
		writeRow(module, tableName, action, recordId, recordName, loginUser, null, remarks, null, null, null);
	}

	@Transactional
	public void saveAdminAudit(String module, String tableName, String action, String recordId, String recordName,
			String loginUser, String remarks, String fieldName, Object oldValue, Object newValue) {
		if (java.util.Objects.equals(oldValue, newValue)) {
			return;
		}
		writeRow(module, tableName, action, recordId, recordName, loginUser, null, remarks, fieldName,
				oldValue == null ? null : String.valueOf(oldValue), newValue == null ? null : String.valueOf(newValue));
	}
}
