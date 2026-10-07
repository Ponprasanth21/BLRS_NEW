package com.bornfire.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_ReminderGenerator_Repo extends JpaRepository<BLRS_ReminderGenerator_Entity, String> {

	@Query("SELECT r FROM BLRS_ReminderGenerator_Entity r ORDER BY r.acct_num")
	List<BLRS_ReminderGenerator_Entity> getReminderList();

	@Query("SELECT r FROM BLRS_ReminderGenerator_Entity r WHERE r.acct_num IN :accts")
	List<BLRS_ReminderGenerator_Entity> getSelectedReminders(@Param("accts") List<String> accts);
}