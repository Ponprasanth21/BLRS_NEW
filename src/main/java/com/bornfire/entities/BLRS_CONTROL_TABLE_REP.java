package com.bornfire.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_CONTROL_TABLE_REP extends JpaRepository<BLRS_Control_Table, Date> {

	@Query(value = "SELECT * FROM public.\"BLRS_CONTROL_TABLE\"", nativeQuery = true)
	BLRS_Control_Table getTranDate();

	@Query(value = "SELECT * FROM BLRS_CONTROL_TABLE", nativeQuery = true)
	List<BLRS_Control_Table> findAll();

	@Query(value = "SELECT DCP_STATUS FROM BLRS_CONTROL_TABLE", nativeQuery = true)
	String getDcpstatus();

}
