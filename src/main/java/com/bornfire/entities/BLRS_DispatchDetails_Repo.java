package com.bornfire.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_DispatchDetails_Repo
		extends JpaRepository<BLRS_DispatchDetails_Entity, BLRS_DispatchDetails_Entity.PK> {

	@Query("SELECT d FROM BLRS_DispatchDetails_Entity d "
			+ "WHERE d.reminder_type = :type AND d.rem_date = :date ORDER BY d.acc_num")
	List<BLRS_DispatchDetails_Entity> getDispatchList(@Param("type") String type, @Param("date") Date date);

	@Modifying
	@Query("UPDATE BLRS_DispatchDetails_Entity d SET d.postal_ref = :ref, d.postal_ack = :ack "
			+ "WHERE d.acc_num = :acc AND d.reminder_type = :type AND d.rem_date = :date")
	int updateDispatch(@Param("type") String type, @Param("date") Date date, @Param("acc") String acc,
			@Param("ref") String ref, @Param("ack") String ack);
}