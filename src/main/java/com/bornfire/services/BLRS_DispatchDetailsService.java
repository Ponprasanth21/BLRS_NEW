package com.bornfire.services;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_DispatchDetails_Entity;
import com.bornfire.entities.BLRS_DispatchDetails_Repo;

@Service
public class BLRS_DispatchDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(BLRS_DispatchDetailsService.class);

	@Autowired
	private BLRS_DispatchDetails_Repo dispatchRepo;

	public List<BLRS_DispatchDetails_Entity> getDispatchList(String reminderType, Date reminderDate) {
		return dispatchRepo.getDispatchList(reminderType, reminderDate);
	}

	@Transactional
	public String saveDispatch(String reminderType, Date reminderDate, String accNum, String postalRef,
			String postalAck, String user) {
		int rows = dispatchRepo.updateDispatch(reminderType, reminderDate, accNum, trim(postalRef), trim(postalAck));
		logger.info("Dispatch details updated by {} for account {} ({} row)", user, accNum, rows);
		return rows > 0 ? "Data Saved Successfully" : "Record not found. Please search again.";
	}

	private String trim(String s) {
		return s == null ? null : s.trim();
	}
}