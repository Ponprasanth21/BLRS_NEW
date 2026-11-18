package com.bornfire.services;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_Access_Role_Entity;
import com.bornfire.entities.BLRS_Access_Role_Repo;

@Service
@Transactional
public class BLRS_AccessRoleService {

	private static final Logger logger = LoggerFactory.getLogger(BLRS_AccessRoleService.class);

	@Autowired
	private BLRS_Access_Role_Repo accessRoleRepo;

	@Autowired
	private BLRS_AuditService auditService;
	
	private static final String AUDIT_MODULE = "ACCESS ROLE";
	private static final String AUDIT_TABLE = "BLRS_ACCESS_ROLE_TABLE";

	/** system fields - not shown as "changes" */
	private static final Set<String> AUDIT_SKIP = new HashSet<>(Arrays.asList("entity_flg", "auth_flg", "modify_flg",
			"del_flg", "entry_user", "modify_user", "auth_user", "entry_time", "modify_time", "auth_time",
			"new_role_flg"));
	private static final Set<String> AUDIT_LIST_FIELDS = new HashSet<>(Arrays.asList("menulist"));
	
	public List<BLRS_Access_Role_Entity> getRoleList() {
		return accessRoleRepo.getAllActiveRoles();
	}

	public BLRS_Access_Role_Entity getRole(String roleId) {
		if (roleId == null || roleId.trim().isEmpty()) {
			return new BLRS_Access_Role_Entity();
		}
		BLRS_Access_Role_Entity role = accessRoleRepo.getRole(roleId.trim());
		if (role == null) {
			Optional<BLRS_Access_Role_Entity> opt = accessRoleRepo.findById(roleId.trim());
			if (opt.isPresent()) {
				role = opt.get();
			} else {
				role = new BLRS_Access_Role_Entity();
			}
		}
		return role;
	}

	public List<String> getRoleIds() {
		return accessRoleRepo.getRoleIds();
	}

	public String addRole(BLRS_Access_Role_Entity role, String formmode, String loginUser) {
		String msg = "";
		try {
			if (role == null || role.getRole_id() == null || role.getRole_id().trim().isEmpty()) {
				return "Role ID cannot be empty";
			}
			role.setRole_id(role.getRole_id().trim());

			if (formmode == null || formmode.trim().isEmpty()) {
				formmode = "add";
			}

			if (formmode.equalsIgnoreCase("add")) {
				Optional<BLRS_Access_Role_Entity> existing = accessRoleRepo.findById(role.getRole_id());
				if (existing.isPresent() && !"Y".equalsIgnoreCase(existing.get().getDel_flg())) {
					return "Role ID Already Exists";
				}

				role.setEntity_flg("N");
				role.setModify_flg("N");
				role.setDel_flg("N");
				role.setNew_role_flg("Y");
				role.setEntry_user(loginUser);
				role.setEntry_time(new Date());

				accessRoleRepo.save(role);
				auditService.saveRecord(AUDIT_MODULE, AUDIT_TABLE, "ADD", role.getRole_id(), role.getRole_desc(), loginUser,
						null, "Role Created Successfully");

				msg = "Role Created Successfully";
			} else if (formmode.equalsIgnoreCase("edit")) {
				BLRS_Access_Role_Entity existing = getRole(role.getRole_id());

				role.setEntity_flg("N");
				role.setModify_flg("Y");
				role.setDel_flg("N");
				role.setEntry_user(existing.getEntry_user());
				role.setEntry_time(existing.getEntry_time());
				role.setModify_user(loginUser);
				role.setModify_time(new Date());

				// snapshot BEFORE save(): 'existing' is a managed entity and save() overwrites it
				Map<String, String> before = auditService.snapshot(existing, AUDIT_SKIP);
				Map<String, String> after = auditService.snapshot(role, AUDIT_SKIP);

				accessRoleRepo.save(role);
				auditService.saveChanges(AUDIT_MODULE, AUDIT_TABLE, "MODIFY", role.getRole_id(), role.getRole_desc(),
						loginUser, "Role Modified Successfully", before, after, null, AUDIT_LIST_FIELDS);
				msg = "Role Modified Successfully";
			}
		} catch (Exception e) {
			logger.error("Error saving role: ", e);
			msg = "Error Occurred while saving Role";
		}
		return msg;
	}

	public String verifyRole(String roleId, String loginUser) {
		String msg = "";
		try {
			if (roleId == null || roleId.trim().isEmpty()) {
				return "Role ID is required";
			}
			BLRS_Access_Role_Entity role = getRole(roleId.trim());
			if (role != null && role.getRole_id() != null) {

				String modifyUser = role.getModify_user();
				String entryUser = role.getEntry_user();

				if (modifyUser != null && !modifyUser.trim().isEmpty()) {
					if (loginUser != null && modifyUser.trim().equalsIgnoreCase(loginUser.trim())) {
						return "Same user cannot verify";
					}
				} else if (entryUser != null && !entryUser.trim().isEmpty()) {
					if (loginUser != null && entryUser.trim().equalsIgnoreCase(loginUser.trim())) {
						return "Same user cannot verify";
					}
				}

				role.setEntity_flg("Y");
				role.setModify_flg("N");
				role.setAuth_user(loginUser);
				role.setAuth_time(new Date());

				accessRoleRepo.save(role);
				auditService.saveRecord(AUDIT_MODULE, AUDIT_TABLE, "VERIFY", role.getRole_id(), role.getRole_desc(),
						loginUser, loginUser, "Role Verified Successfully");

				msg = "Role Verified Successfully";
			} else {
				msg = "Role Not Found";
			}
		} catch (Exception e) {
			logger.error("Error verifying role: ", e);
			msg = "Error Occurred while verifying Role";
		}
		return msg;
	}

	public String deleteRole(String roleId, String loginUser) {
		String msg = "";
		try {
			if (roleId == null || roleId.trim().isEmpty()) {
				return "Role ID is required";
			}
			BLRS_Access_Role_Entity role = getRole(roleId.trim());
			if (role != null && role.getRole_id() != null) {
				role.setDel_flg("Y");
				role.setModify_user(loginUser);
				role.setModify_time(new Date());

				accessRoleRepo.save(role);
				
				auditService.saveRecord(AUDIT_MODULE, AUDIT_TABLE, "DELETE", role.getRole_id(), role.getRole_desc(),
						loginUser, null, "Role Deleted Successfully");


				msg = "Role Deleted Successfully";
			} else {
				msg = "Role Not Found";
			}
		} catch (Exception e) {
			logger.error("Error deleting role: ", e);
			msg = "Error Occurred while deleting Role";
		}
		return msg;
	}
}
