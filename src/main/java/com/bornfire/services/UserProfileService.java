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

@Service
@Transactional
public class UserProfileService {

	private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

	@Autowired
	private BLRS_UserProfile_Repo userProfileRep;

	@Value("${default.password:Bornfire@123}")
	private String defaultPassword;

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

			userProfileRep.save(userProfile);
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
