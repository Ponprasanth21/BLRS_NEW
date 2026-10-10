
package com.bornfire.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_LoanAccountProfile_Entity;
import com.bornfire.entities.BLRS_LoanAccountProfile_Repo;

@Service
@Transactional(readOnly = true)
public class BLRS_LoanAccountProfileService {

    private final BLRS_LoanAccountProfile_Repo blrsLoanAccountProfileRepo;

    @Autowired
    public BLRS_LoanAccountProfileService(
            BLRS_LoanAccountProfile_Repo blrsLoanAccountProfileRepo) {
        this.blrsLoanAccountProfileRepo = blrsLoanAccountProfileRepo;
    }

    public List<BLRS_LoanAccountProfile_Entity> getAllLoanDetails() {
        return blrsLoanAccountProfileRepo.findAll();
    }

    public BLRS_LoanAccountProfile_Entity getLoanDetailsByCustomerId(
            String customerId) {
        return blrsLoanAccountProfileRepo.findByCustomerId(customerId);
    }
}
