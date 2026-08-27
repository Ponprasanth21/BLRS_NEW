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

	@Query("SELECT u FROM BLRS_UserProfile_Entity u WHERE (u.del_flg IS NULL OR u.del_flg != 'Y') ORDER BY u.userid ASC")
	List<BLRS_UserProfile_Entity> getAllList();

	@Query("SELECT u FROM BLRS_UserProfile_Entity u WHERE u.userid = :userId")
	BLRS_UserProfile_Entity getRole(@Param("userId") String userId);

	@Query("SELECT u FROM BLRS_UserProfile_Entity u WHERE u.entity_flg = :entityFlg AND (u.del_flg IS NULL OR u.del_flg != 'Y') ORDER BY u.userid ASC")
	List<BLRS_UserProfile_Entity> getListByEntityFlg(@Param("entityFlg") String entityFlg);

	@Query("SELECT u FROM BLRS_UserProfile_Entity u WHERE u.userid = :userid")
	Optional<BLRS_UserProfile_Entity> findUserByUserId(@Param("userid") String userid);

	@Modifying
	@Transactional
	@Query("UPDATE BLRS_UserProfile_Entity a SET a.no_of_attmp = COALESCE(a.no_of_attmp, 0) + 1, a.user_locked_flg = CASE WHEN COALESCE(a.no_of_attmp, 0) + 1 >= 3 THEN 'Y' ELSE 'N' END, a.login_status = CASE WHEN COALESCE(a.no_of_attmp, 0) + 1 >= 3 THEN 'Inactive' ELSE 'Active' END WHERE a.userid = :userid")
	int updateFailedLogin(@Param("userid") String userid);
}