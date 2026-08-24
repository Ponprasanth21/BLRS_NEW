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
public interface BGLSBusinessTable_Rep extends JpaRepository<BGLSBusinessTable_Entity, String>{
	@Query(value = "select * from BGLS_BUSINESS_TABLE", nativeQuery = true)
	List<BGLSBusinessTable_Entity> getauditListLocalvalues();
	
	@Query(value = "select * from BGLS_BUSINESS_TABLE where audit_date = ?1", nativeQuery = true)
	List<BGLSBusinessTable_Entity> getauditListOpeartion(Date audit_date);
	
	@Query(value = "SELECT BGLS_BUSINESS_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	Long getAuditRefUUID();
	
	@Query(value = "SELECT * FROM BGLS_BUSINESS_TABLE WHERE TRUNC(audit_date) = TRUNC(?1)", nativeQuery = true)
	List<BGLSBusinessTable_Entity> getauditListLocalvaluesbusiness(Date fromDateToUse);
	
}
