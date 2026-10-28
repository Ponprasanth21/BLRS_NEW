
package com.bornfire.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_Guarantor_Entity;
import com.bornfire.entities.BLRS_Guarantor_Repo;
import com.bornfire.entities.BLRS_LoanAccountProfile_Entity;
import com.bornfire.entities.BLRS_LoanAccountProfile_Repo;

@Service
@Transactional(readOnly = true)
public class BLRS_LoanAccountProfileService {

    private final BLRS_LoanAccountProfile_Repo blrsLoanAccountProfileRepo;
    
    private final BLRS_Guarantor_Repo blrsGuarantorRepo;
    @Autowired
    public BLRS_LoanAccountProfileService(
            BLRS_LoanAccountProfile_Repo blrsLoanAccountProfileRepo,
            BLRS_Guarantor_Repo blrsGuarantorRepo) {
        this.blrsLoanAccountProfileRepo = blrsLoanAccountProfileRepo;
        this.blrsGuarantorRepo = blrsGuarantorRepo;
    }

    public List<BLRS_LoanAccountProfile_Entity> getAllLoanDetails() {
        return blrsLoanAccountProfileRepo.findAll();
    }

    public BLRS_LoanAccountProfile_Entity getLoanDetailsByCustomerId(
            String customerId) {
        return blrsLoanAccountProfileRepo.findByCustomerId(customerId);
    }
    
    // NEW : guarantors of one loan account
    public List<BLRS_Guarantor_Entity> getGuarantorsByAccountNumber(String accountNumber) {
        return blrsGuarantorRepo.findByAccountNumberOrderByGuarantorSlNoAsc(accountNumber);
    }
}
