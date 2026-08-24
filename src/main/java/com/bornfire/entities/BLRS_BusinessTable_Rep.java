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
public interface BLRS_BusinessTable_Rep extends JpaRepository<BLRS_BusinessTable_Entity, String> {
	@Query(value = "select * from BLRS_BUSINESS_TABLE", nativeQuery = true)
	List<BLRS_BusinessTable_Entity> getauditListLocalvalues();

	@Query(value = "select * from BLRS_BUSINESS_TABLE where audit_date = ?1", nativeQuery = true)
	List<BLRS_BusinessTable_Entity> getauditListOpeartion(Date audit_date);

	@Query(value = "SELECT nextval('public.\"BLRS_BUSINESS_SEQ\"')", nativeQuery = true)
	Long getBusinessRefUUID();

	@Query(value = "SELECT * FROM BLRS_BUSINESS_TABLE WHERE TRUNC(audit_date) = TRUNC(?1)", nativeQuery = true)
	List<BLRS_BusinessTable_Entity> getauditListLocalvaluesbusiness(Date fromDateToUse);

}
