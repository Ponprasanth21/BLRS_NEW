package com.bornfire.entities;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_UserProfile_Repo extends JpaRepository<BLRS_UserProfile_Entity, String> {

	@Query(value = "SELECT * FROM BLRS_USER_PROFILE WHERE DEL_FLG = 'N' ORDER BY SRL_NO ASC", nativeQuery = true)
	List<BLRS_UserProfile_Entity> getAllList();

	@Query(value = "select * from BLRS_USER_PROFILE where USER_ID=?1", nativeQuery = true)
	BLRS_UserProfile_Entity getRole(String userId);

	@Query(value = "SELECT NVL(MAX(SRL_NO),0)+1 FROM BLRS_USER_PROFILE", nativeQuery = true)
	Integer getMaxSrlNo();

	@Query(value = "SELECT * FROM BLRS_USER_PROFILE WHERE ENTITY_FLG=?1 AND DEL_FLG='N' ORDER BY REGEXP_SUBSTR(USER_ID, '^[^0-9]+'), TO_NUMBER(REGEXP_SUBSTR(USER_ID, '[0-9]+'))", nativeQuery = true)
	List<BLRS_UserProfile_Entity> getListByEntityFlg(String entityFlg);

	@Query(value = "SELECT * " + "FROM public.\"BLRS_USER_PROFILE\" "
			+ "WHERE \"USER_ID\" = :userid", nativeQuery = true)
	Optional<BLRS_UserProfile_Entity> findUserByUserId(@Param("userid") String userid);

	@Modifying
	@Transactional
	@Query("UPDATE BLRS_UserProfile_Entity a " + "SET a.no_of_attmp = COALESCE(a.no_of_attmp, 0) + 1, "
			+ "a.user_locked_flg = CASE " + "WHEN COALESCE(a.no_of_attmp, 0) + 1 >= 3 THEN 'Y' " + "ELSE 'N' END, "
			+ "a.login_status = CASE " + "WHEN COALESCE(a.no_of_attmp, 0) + 1 >= 3 THEN 'Inactive' "
			+ "ELSE 'Active' END " + "WHERE a.userid = :userid")
	int updateFailedLogin(@Param("userid") String userid);
}