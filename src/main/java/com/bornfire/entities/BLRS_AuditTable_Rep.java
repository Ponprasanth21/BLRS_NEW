package com.bornfire.entities;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public interface BLRS_AuditTable_Rep extends JpaRepository<BLRS_AuditTable, String> {
	@Query(value = "select * from BLRS_CONTROL_TABLE where trunc(audit_date)=?1  AND audit_table  in ('BGLS_USER_PROFILE_TABLE') and modi_details is not null order by entry_time desc", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocal1(String Fromdate);

	@Query(value = "select * from BLRS_CONTROL_TABLE where trunc(audit_date) between ?1 and ?2  AND audit_table  in ('BGLS_USER_PROFILE_TABLE') and modi_details is not null order by entry_time desc", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocal(Date Fromdate, Date Todate);

	@Query(value = "select * from BLRS_CONTROL_TABLE where trunc(audit_date)= ?1   AND audit_table  in ('BGLS_USER_PROFILE_TABLE') and modi_details is not null order by entry_time desc", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocal(Date Fromdate);

	@Query(value = "select * from BLRS_AUDIT_TABLE where trunc(audit_date) between ?1 and ?2  AND audit_table not in ('BGLS_USER_PROFILE_TABLE') order by audit_date desc", nativeQuery = true)
	List<BLRS_AuditTable> getauditListOpeartion(Date Fromdate, Date Todate);

	@Query(value = "select * from BLRS_AUDIT_TABLE where trunc(audit_date)= ?1  AND audit_table not in ('BGLS_USER_PROFILE_TABLE') order by audit_date desc", nativeQuery = true)
	List<BLRS_AuditTable> getauditListOpeartion(Date Fromdate);

	@Query(value = "select * from BLRS_AUDIT_TABLE", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocalvals();

	@Query(value = "SELECT * FROM BLRS_AUDIT_TABLE WHERE TRUNC(audit_date) = TRUNC(?1)", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocalvals(Date fromDateToUse);

	@Query(value = "select * from BLRS_AUDIT_TABLE where audit_date = ?1", nativeQuery = true)
	List<BLRS_AuditTable> getauditListLocalvalues(Date audit_date);

	// Generate Request_UUID
	@Query(value = "SELECT nextval('public.\"BLRS_AUDIT_SEQ\"')", nativeQuery = true)
	Long getAuditRefUUID();

	@Query(value = "SELECT DISTINCT(branch_des) FROM BLRS_AUDIT_TABLE where BRANCH_ID =?1 AND DEL_FLG='N'", nativeQuery = true)
	String getBranchName(String branch_id);

}
