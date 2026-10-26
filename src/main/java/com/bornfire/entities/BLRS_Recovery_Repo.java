package com.bornfire.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_Recovery_Repo extends JpaRepository<BLRS_Recovery_Entity, String> {

    List<BLRS_Recovery_Entity> findByAccountNumberOrderByRecoveryDateDesc(String accountNumber);

    List<BLRS_Recovery_Entity> findByRecoveryDateBetween(Date from, Date to);
}
