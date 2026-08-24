package com.bornfire.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_Access_Role_Repo extends JpaRepository<BLRS_Access_Role_Entity, String> {

	
	@Query(value = "select * from BLRS_ACCESS_ROLE_TABLE where USER_ID=?1", nativeQuery = true)
	BLRS_Access_Role_Entity getRole(String userId);
}
