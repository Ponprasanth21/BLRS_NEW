package com.bornfire.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.bornfire.entities.BLRS_UserProfile_Entity;
import com.bornfire.entities.BLRS_UserProfile_Repo;

@Service
@ConfigurationProperties("output")
@Transactional
public class ListofDataService {
	
	@Autowired
	BLRS_UserProfile_Repo userProfileRep;
	

	public List<BLRS_UserProfile_Entity> getUsersList() {
	    List<BLRS_UserProfile_Entity> userList = userProfileRep.getAllList();
	    return userList;
	}
}
