package com.bornfire.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_Loanee_Repo extends JpaRepository<BLRS_Loanee_Entity, BLRS_Loanee_Entity.PK> {

    List<BLRS_Loanee_Entity> findByAccountNumberOrderByLoaneeIndexAsc(String accountNumber);

    BLRS_Loanee_Entity findByAccountNumberAndLoaneeIndex(String accountNumber, Integer loaneeIndex);

    void deleteByAccountNumber(String accountNumber);
}
