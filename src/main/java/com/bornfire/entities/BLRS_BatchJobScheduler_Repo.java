package com.bornfire.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_BatchJobScheduler_Repo extends JpaRepository<BLRS_BatchJobScheduler_Entity, String> {

	// List screen : deleted (verified delete) jobs are hidden
	@Query("SELECT j FROM BLRS_BatchJobScheduler_Entity j WHERE j.del_flg IS NULL OR j.del_flg <> 'Y' ORDER BY j.job_id")
	List<BLRS_BatchJobScheduler_Entity> getJobList();

	@Query("SELECT j FROM BLRS_BatchJobScheduler_Entity j WHERE j.job_id = :jobId")
	BLRS_BatchJobScheduler_Entity getJob(@Param("jobId") String jobId);

	// Next Process Id : BJ000, BJ001 ... (only BJnnn ids are considered, FIN001 / DRS002 are ignored)
	@Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(JOB_ID FROM 3) AS INTEGER)), -1) + 1 "
			+ "FROM BLRS_BATCH_JOB_SCHEDULER_TAB WHERE JOB_ID ~ '^BJ[0-9]+$'", nativeQuery = true)
	Number getNextJobSeq();

	// Duplicate job name check
	@Query("SELECT COUNT(j) FROM BLRS_BatchJobScheduler_Entity j "
			+ "WHERE UPPER(j.job_name) = UPPER(:name) AND (j.del_flg IS NULL OR j.del_flg <> 'Y')")
	long countActiveByName(@Param("name") String name);

	// Drop-down sources (merged with the default values in the service)
	@Query("SELECT DISTINCT j.department FROM BLRS_BatchJobScheduler_Entity j WHERE j.department IS NOT NULL")
	List<String> getDepartmentValues();

	@Query("SELECT DISTINCT j.schm_code FROM BLRS_BatchJobScheduler_Entity j WHERE j.schm_code IS NOT NULL")
	List<String> getSchemeValues();
}