package com.bornfire.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.config.AES;
import com.bornfire.entities.BLRS_UserProfile_Entity;
import com.bornfire.entities.BLRS_UserProfile_Repo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


import com.bornfire.config.SequenceGenerator;
import com.bornfire.entities.BLRS_AuditTable;
import com.bornfire.entities.BLRS_AuditTable_Rep;


@Service
@Transactional
public class UserProfileService {

	private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

	@Autowired
	private BLRS_UserProfile_Repo userProfileRep;
	
	@Autowired
	private BLRS_AuditService auditService;
	
	@Autowired
	private BLRS_AuditTable_Rep auditTableRep;

	@Autowired
	private SequenceGenerator sequence;

	@Value("${default.password:Bornfire@123}")
	private String defaultPassword;
	
	// ---------------------------------------------------------------------------------
	// AUDIT HELPERS
	// ---------------------------------------------------------------------------------

	/** Fields that are system generated / sensitive and are not shown as "changes". */
	private static final Set<String> AUDIT_SKIP_FIELDS = new HashSet<>(Arrays.asList("serialVersionUID", "password",
			"photo", "entry_user", "entry_time", "modify_user", "modify_time", "modify_flg", "entity_flg", "auth_user",
			"auth_time", "auth_flg", "del_flg", "no_of_attmp", "session_id", "login_flg", "user_locked_flg", "disable_flg",
			"new_user_flg"));

	private static final int AUDIT_MAX_LEN = 250;

	/** Converts a field value to a comparable / displayable text. */
	private String auditText(Object v) {
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

	/** "role_id" -> "Role Id" */
	private String auditLabel(String fieldName) {
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

	private String cut(String v) {
		if (v == null) {
			return null;
		}
		return v.length() > AUDIT_MAX_LEN ? v.substring(0, AUDIT_MAX_LEN - 3) + "..." : v;
	}

	/**
	 * Compares the stored user with the edited user and returns one entry per
	 * changed field: [fieldName, oldValue, newValue].
	 */
	private List<String[]> findChanges(BLRS_UserProfile_Entity oldUser, BLRS_UserProfile_Entity newUser) {
		List<String[]> changes = new ArrayList<>();
		for (Field f : BLRS_UserProfile_Entity.class.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) || AUDIT_SKIP_FIELDS.contains(f.getName())) {
				continue;
			}
			try {
				f.setAccessible(true);
				String o = auditText(f.get(oldUser));
				String n = auditText(f.get(newUser));
				if (!o.equals(n)) {
					changes.add(new String[] { auditLabel(f.getName()), o.isEmpty() ? "-" : o, n.isEmpty() ? "-" : n });
				}
			} catch (Exception e) {
				logger.error("Audit compare failed for field " + f.getName(), e);
			}
		}
		return changes;
	}

	/** Writes one row to the audit table (shown in User Activity Audits). */
	private void writeAudit(String funcCode, BLRS_UserProfile_Entity target, String entryUser, String authUser,
			String fieldName, String oldValue, String newValue, String remarks) {
		try {
			BLRS_AuditTable audit = new BLRS_AuditTable();
			Date now = new Date();
			audit.setAudit_ref_no(sequence.generateRequestUUId());
			audit.setAudit_date(now);
			audit.setEntry_time(now);
			audit.setEntry_user(entryUser);
			audit.setAuth_user(authUser);
			audit.setAuth_time(authUser != null ? now : null);
			audit.setFunc_code(funcCode);
			audit.setAudit_table("BLRS_USER_PROFILE");
			audit.setAudit_screen("USER PROFILE - " + funcCode);
			audit.setEvent_id(target.getUserid());
			audit.setEvent_name(target.getUsername());
			audit.setField_name(cut(fieldName));
			audit.setOld_value(cut(oldValue));
			audit.setNew_value(cut(newValue));
			audit.setModi_details(remarks);
			audit.setRemarks(remarks);
			auditTableRep.save(audit);
		} catch (Exception e) {
			// an audit problem must never block the user operation itself
			logger.error("Audit write failed for user " + target.getUserid(), e);
		}
	}


	public List<BLRS_UserProfile_Entity> getUsersList() {
		return userProfileRep.getAllList();
	}

	public BLRS_UserProfile_Entity getUser(String id) {
		if (id != null && !id.trim().isEmpty()) {
			Optional<BLRS_UserProfile_Entity> opt = userProfileRep.findUserByUserId(id.trim());
			if (opt.isPresent()) {
				return opt.get();
			}
			return userProfileRep.findById(id.trim()).orElse(new BLRS_UserProfile_Entity());
		}
		return new BLRS_UserProfile_Entity();
	}

	public String addUser(BLRS_UserProfile_Entity userProfile, String formmode, String inputUser) {

		String msg = "";
		try {
			if (userProfile == null || userProfile.getUserid() == null || userProfile.getUserid().trim().isEmpty()) {
				return "User ID cannot be empty";
			}
			userProfile.setUserid(userProfile.getUserid().trim());

			if (formmode == null || formmode.trim().isEmpty()) {
				formmode = "add";
			}

			if ("add".equalsIgnoreCase(formmode)) {
				Optional<BLRS_UserProfile_Entity> existing = userProfileRep.findById(userProfile.getUserid());
				if (existing.isPresent() && !"Y".equalsIgnoreCase(existing.get().getDel_flg())) {
					return "User ID already exists!";
				}

				String rawPassword = (userProfile.getPassword() != null && !userProfile.getPassword().isEmpty())
						? userProfile.getPassword()
						: this.defaultPassword;

				String encryptedPassword = AES.encrypt(rawPassword);
				userProfile.setPassword(encryptedPassword);

			if ("Active".equalsIgnoreCase(userProfile.getLogin_status())) {
				userProfile.setUser_locked_flg("N");
			} else {
				userProfile.setUser_locked_flg("Y");
			}

			if ("Active".equalsIgnoreCase(userProfile.getUser_status())) {
				userProfile.setDisable_flg("N");
			} else {
				userProfile.setDisable_flg("Y");
			}

			userProfile.setEntity_flg("N");
			userProfile.setNew_user_flg("Y");
			userProfile.setModify_flg("N");
			userProfile.setDel_flg("N");
			userProfile.setLogin_flg("N");
			userProfile.setNo_of_attmp(0);
			userProfile.setEntry_time(new Date());
			userProfile.setEntry_user(inputUser);

			userProfileRep.save(userProfile);

			writeAudit("ADD", userProfile, inputUser, null, "-", null, null, "User Created Successfully");
			msg = "User Created Successfully";

		} else if ("edit".equalsIgnoreCase(formmode) || "modify".equalsIgnoreCase(formmode)) {
			Optional<BLRS_UserProfile_Entity> opt = userProfileRep.findById(userProfile.getUserid());
			if (!opt.isPresent()) {
				return "User Not Found";
			}

			BLRS_UserProfile_Entity original = opt.get();

			userProfile.setPassword(original.getPassword());
			userProfile.setEntry_user(original.getEntry_user());
			userProfile.setEntry_time(original.getEntry_time());
			userProfile.setModify_user(inputUser);
			userProfile.setModify_time(new Date());
			userProfile.setModify_flg("Y");
			userProfile.setEntity_flg("N");
			userProfile.setDel_flg("N");
			userProfile.setNo_of_attmp(0);

			if ("Active".equalsIgnoreCase(userProfile.getLogin_status())) {
				userProfile.setUser_locked_flg("N");
			} else {
				userProfile.setUser_locked_flg("Y");
			}

			if ("Active".equalsIgnoreCase(userProfile.getUser_status())) {
				userProfile.setDisable_flg("N");
			} else {
				userProfile.setDisable_flg("Y");
			}

			if (userProfile.getPhoto() == null || userProfile.getPhoto().length == 0) {
				userProfile.setPhoto(original.getPhoto());
			}

			// Collect old/new values BEFORE save(): 'original' is a managed entity and
						// save() would overwrite it with the new values.
						List<String[]> changes = findChanges(original, userProfile);

						userProfileRep.save(userProfile);

						if (changes.isEmpty()) {
							writeAudit("MODIFY", userProfile, inputUser, null, "-", null, null, "User Modified Successfully");
						} else {
							for (String[] c : changes) {
								writeAudit("MODIFY", userProfile, inputUser, null, c[0], c[1], c[2], "User Modified Successfully");
							}
						}
			msg = "User Modified Successfully";
		}
		} catch (Exception e) {
			logger.error("Error in addUser: ", e);
			msg = "Error occurred while saving user profile";
		}

		return msg;
	}

	public String verifyUser(String userId, String inputUser) {
		String msg = "";

		if (userId == null || userId.trim().isEmpty()) {
			return "User ID is required";
		}

		Optional<BLRS_UserProfile_Entity> opt = userProfileRep.findById(userId.trim());
		if (opt.isPresent()) {
			BLRS_UserProfile_Entity userProfile = opt.get();

			String modifyUser = userProfile.getModify_user();
			String entryUser = userProfile.getEntry_user();

			if (modifyUser != null && !modifyUser.trim().isEmpty()) {
				if (inputUser != null && modifyUser.trim().equalsIgnoreCase(inputUser.trim())) {
					return "Same user cannot verify";
				}
			} else if (entryUser != null && !entryUser.trim().isEmpty()) {
				if (inputUser != null && entryUser.trim().equalsIgnoreCase(inputUser.trim())) {
					return "Same user cannot verify";
				}
			}

			userProfile.setEntity_flg("Y");
			userProfile.setModify_flg("N");
			userProfile.setAuth_user(inputUser);
			userProfile.setAuth_time(new Date());
			userProfileRep.save(userProfile);
			
			writeAudit("VERIFY", userProfile, inputUser, inputUser, "-", null, null, "User Verified Successfully");


			msg = "User Verified Successfully";
		} else {
			msg = "User Not Found";
		}

		return msg;
	}

	public String deleteUser(String userId, String deleteType, String inputUser) {
		String msg = "";

		if (userId == null || userId.trim().isEmpty()) {
			return "User ID is required";
		}

		Optional<BLRS_UserProfile_Entity> opt = userProfileRep.findById(userId.trim());
		if (opt.isPresent()) {
			BLRS_UserProfile_Entity userProfile = opt.get();
			userProfile.setDel_flg("Y");
			userProfile.setModify_user(inputUser);
			userProfile.setModify_time(new Date());
			userProfileRep.save(userProfile);
			writeAudit("DELETE", userProfile, inputUser, null, "-", null, null, "User Deleted Successfully");



			msg = "User Deleted Successfully";
		} else {
			msg = "User Not Found";
		}

		return msg;
	}

	public String cancelUser(String userId, String inputUser) {
		String msg = "";
		if (userId == null || userId.trim().isEmpty()) {
			return "User ID is required";
		}

		Optional<BLRS_UserProfile_Entity> opt = userProfileRep.findById(userId.trim());
		if (opt.isPresent()) {
			BLRS_UserProfile_Entity userProfile = opt.get();
			if ("Y".equalsIgnoreCase(userProfile.getNew_user_flg()) && !"Y".equalsIgnoreCase(userProfile.getEntity_flg())) {
				userProfileRep.deleteById(userId.trim());
			} else {
				userProfile.setModify_flg("N");
				userProfileRep.save(userProfile);
			}

			msg = "User Modification Cancelled Successfully";
		} else {
			msg = "User Not Found";
		}
		return msg;
	}

	public String passwordReset(String targetUserId, String newPass, String inputUser) {
		String msg = "";
		try {
			if (targetUserId == null || targetUserId.trim().isEmpty()) {
				return "User ID is required";
			}

			String rawPass = (newPass != null && !newPass.trim().isEmpty()) ? newPass : this.defaultPassword;
			String encryptedPassword = AES.encrypt(rawPass);

			Optional<BLRS_UserProfile_Entity> opt = userProfileRep.findById(targetUserId.trim());
			if (opt.isPresent()) {
				BLRS_UserProfile_Entity user = opt.get();
				user.setPassword(encryptedPassword);
				user.setModify_user(inputUser);
				user.setModify_time(new Date());
				user.setNo_of_attmp(0);
				user.setLogin_flg("N");
				user.setUser_locked_flg("N");
				user.setNew_user_flg("Y");

				userProfileRep.save(user);
				msg = "Password Resetted Successfully";
			} else {
				msg = "User Not Found";
			}
		} catch (Exception e) {
			logger.error("Error in passwordReset: ", e);
			msg = "Error Occurred while resetting password";
		}
		return msg;
	}

	public String changePassword(String old_password, String new_password, String userid) {
		try {
			if (userid == null || userid.trim().isEmpty()) {
				return "User ID is required";
			}

			Optional<BLRS_UserProfile_Entity> opt = userProfileRep.findById(userid.trim());
			if (!opt.isPresent()) {
				return "User Not Found";
			}
			BLRS_UserProfile_Entity user = opt.get();

			if (!AES.validatePassword(old_password, user.getPassword())) {
				return "Current password does not match!";
			}

			String encNewPass = AES.encrypt(new_password);
			user.setPassword(encNewPass);
			user.setModify_user(userid.trim());
			user.setModify_time(new Date());
			user.setNew_user_flg("N");
			userProfileRep.save(user);

			return "Password changed successfully!";
		} catch (Exception e) {
			logger.error("Error changing password: ", e);
			return "Error occurred while changing password";
		}
	}

	public String checkPasswordChangeReq(String userid) {
		if (userid != null && userProfileRep.existsById(userid.trim())) {
			Optional<BLRS_UserProfile_Entity> up = userProfileRep.findById(userid.trim());
			if (up.isPresent()) {
				return up.get().getLogin_flg();
			}
		}
		return "N";
	}
}
