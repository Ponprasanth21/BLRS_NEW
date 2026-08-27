package com.bornfire.entities;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_Access_Role_Repo extends JpaRepository<BLRS_Access_Role_Entity, String> {

	@Query("SELECT a FROM BLRS_Access_Role_Entity a WHERE a.role_id = :roleId")
	BLRS_Access_Role_Entity getRole(@Param("roleId") String roleId);

	@Query("SELECT a FROM BLRS_Access_Role_Entity a WHERE (a.del_flg IS NULL OR a.del_flg != 'Y') ORDER BY a.role_id ASC")
	List<BLRS_Access_Role_Entity> getAllActiveRoles();

	@Query("SELECT DISTINCT a.role_id FROM BLRS_Access_Role_Entity a WHERE (a.del_flg IS NULL OR a.del_flg != 'Y')")
	List<String> getRoleIds();
}

