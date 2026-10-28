package com.bornfire.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_Guarantor_Repo extends JpaRepository<BLRS_Guarantor_Entity, String> {

    // All guarantors of one loan account, in serial order
    List<BLRS_Guarantor_Entity> findByAccountNumberOrderByGuarantorSlNoAsc(String accountNumber);
}