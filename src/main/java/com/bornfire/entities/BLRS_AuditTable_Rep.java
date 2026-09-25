package com.bornfire.entities;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public interface BLRS_AuditTable_Rep extends JpaRepository<BLRS_AuditTable, String> {

	@Query(value = "SELECT * FROM BLRS_CONTROL_TABLE " + "WHERE CAST(audit_date AS DATE) = CAST(?1 AS DATE) "
			+ "AND audit_table IN ('BLRS_USER_PROFILE') " + "AND modi_details IS NOT NULL "
			+ "ORDER BY entry_time DESC", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocal1(String Fromdate);

	@Query(value = "SELECT * FROM BLRS_CONTROL_TABLE "
			+ "WHERE CAST(audit_date AS DATE) BETWEEN CAST(?1 AS DATE) AND CAST(?2 AS DATE) "
			+ "AND audit_table IN ('BLRS_USER_PROFILE') " + "AND modi_details IS NOT NULL "
			+ "ORDER BY entry_time DESC", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocal(Date Fromdate, Date Todate);

	@Query(value = "SELECT * FROM BLRS_CONTROL_TABLE " + "WHERE CAST(audit_date AS DATE) = CAST(?1 AS DATE) "
			+ "AND audit_table IN ('BLRS_USER_PROFILE') " + "AND modi_details IS NOT NULL "
			+ "ORDER BY entry_time DESC", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocal(Date Fromdate);

	@Query(value = "SELECT * FROM BLRS_AUDIT_TABLE "
			+ "WHERE CAST(audit_date AS DATE) BETWEEN CAST(?1 AS DATE) AND CAST(?2 AS DATE) "
			+ "AND (audit_screen IN ('LOGIN', 'LOGOUT') " + "     OR audit_table NOT IN ('BLRS_USER_PROFILE')) "
			+ "ORDER BY audit_date DESC", nativeQuery = true)
	List<BLRS_AuditTable> getauditListOpeartion(Date Fromdate, Date Todate);
	
	@Query(value = "SELECT * FROM BLRS_AUDIT_TABLE "
	        + "WHERE CAST(audit_date AS DATE) BETWEEN CAST(?1 AS DATE) AND CAST(?2 AS DATE) "
	        + "AND audit_screen NOT IN ('LOGIN', 'LOGOUT') "
	        + "AND audit_table NOT IN ('BLRS_USER_PROFILE') "
	        + "ORDER BY audit_date DESC",
	        nativeQuery = true)
	List<BLRS_AuditTable> getbusinessListOpeartion(Date Fromdate, Date Todate);

	@Query(value = "SELECT * FROM BLRS_AUDIT_TABLE " + "WHERE CAST(audit_date AS DATE) = CAST(?1 AS DATE) "
			+ "AND audit_table NOT IN ('BLRS_USER_PROFILE') " + "ORDER BY audit_date DESC", nativeQuery = true)
	List<BLRS_AuditTable> getauditListOpeartion(Date Fromdate);

	@Query(value = "SELECT * FROM BLRS_AUDIT_TABLE", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocalvals();

	@Query(value = "SELECT * FROM BLRS_AUDIT_TABLE "
			+ "WHERE CAST(audit_date AS DATE) = CAST(?1 AS DATE)", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocalvals(Date fromDateToUse);

	@Query(value = "SELECT * FROM BLRS_AUDIT_TABLE " + "WHERE audit_date = ?1", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocalvalues(Date audit_date);

	// Generate Request_UUID
	@Query(value = "SELECT nextval('blrs.blrs_audit_seq')", nativeQuery = true)
	Long getAuditRefUUID();

	@Query(value = "SELECT DISTINCT(branch_des) " + "FROM BLRS_AUDIT_TABLE " + "WHERE BRANCH_ID = ?1 "
			+ "AND DEL_FLG = 'N'", nativeQuery = true)
	String getBranchName(String branch_id);

}