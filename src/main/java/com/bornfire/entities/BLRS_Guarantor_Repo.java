package com.bornfire.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_Guarantor_Repo extends JpaRepository<BLRS_Guarantor_Entity, BLRS_Guarantor_Entity.PK> {

    List<BLRS_Guarantor_Entity> findByAccountNumberOrderByGuarantorIdAsc(String accountNumber);

    List<BLRS_Guarantor_Entity> findByAccountNumberAndStatus(String accountNumber, String status);

    void deleteByAccountNumber(String accountNumber);
}
