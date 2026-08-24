package com.bornfire.entities;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_UserProfile_Repo extends JpaRepository<BLRS_UserProfile_Entity, String> {

	@Query(value = "SELECT * FROM BIPS_USER_PROFILE WHERE DEL_FLG = 'N' ORDER BY SRL_NO ASC", nativeQuery = true)
	List<BLRS_UserProfile_Entity> getAllList();

	@Query(value = "select * from BIPS_USER_PROFILE where USER_ID=?1", nativeQuery = true)
	BLRS_UserProfile_Entity getRole(String userId);

	@Query(value = "SELECT NVL(MAX(SRL_NO),0)+1 FROM BIPS_USER_PROFILE", nativeQuery = true)
	Integer getMaxSrlNo();

	@Query(value = "SELECT * FROM BIPS_USER_PROFILE WHERE ENTITY_FLG=?1 AND DEL_FLG='N' ORDER BY REGEXP_SUBSTR(USER_ID, '^[^0-9]+'), TO_NUMBER(REGEXP_SUBSTR(USER_ID, '[0-9]+'))", nativeQuery = true)
	List<BLRS_UserProfile_Entity> getListByEntityFlg(String entityFlg);
}