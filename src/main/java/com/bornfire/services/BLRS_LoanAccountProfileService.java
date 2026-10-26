
package com.bornfire.services;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_Guarantor_Entity;
import com.bornfire.entities.BLRS_Guarantor_Repo;
import com.bornfire.entities.BLRS_LoanAccountProfile_Entity;
import com.bornfire.entities.BLRS_LoanAccountProfile_Repo;
import com.bornfire.entities.BLRS_Loanee_Entity;
import com.bornfire.entities.BLRS_Loanee_Repo;

@Service
@Transactional(readOnly = true)
public class BLRS_LoanAccountProfileService {

    private final BLRS_LoanAccountProfile_Repo blrsLoanAccountProfileRepo;
    private final BLRS_Loanee_Repo loaneeRepo;
    private final BLRS_Guarantor_Repo guarantorRepo;

    @Autowired
    public BLRS_LoanAccountProfileService(
            BLRS_LoanAccountProfile_Repo blrsLoanAccountProfileRepo,
            BLRS_Loanee_Repo loaneeRepo,
            BLRS_Guarantor_Repo guarantorRepo) {
        this.blrsLoanAccountProfileRepo = blrsLoanAccountProfileRepo;
        this.loaneeRepo = loaneeRepo;
        this.guarantorRepo = guarantorRepo;
    }

    public List<BLRS_LoanAccountProfile_Entity> getAllLoanDetails() {
        List<BLRS_LoanAccountProfile_Entity> list = blrsLoanAccountProfileRepo.findAll();
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public BLRS_LoanAccountProfile_Entity getLoanDetailsByCustomerId(String customerId) {
        return blrsLoanAccountProfileRepo.findByCustomerId(customerId);
    }

    public BLRS_LoanAccountProfile_Entity getLoanDetailsByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return null;
        }
        BLRS_LoanAccountProfile_Entity loan = blrsLoanAccountProfileRepo.findByAccountNumber(accountNumber.trim());
        if (loan == null) {
            loan = blrsLoanAccountProfileRepo.findFirstByAccountNumber(accountNumber.trim());
        }
        if (loan == null) {
            loan = blrsLoanAccountProfileRepo.findByCustomerId(accountNumber.trim());
        }
        return loan;
    }

    public List<BLRS_Loanee_Entity> getLoaneesByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return loaneeRepo.findByAccountNumberOrderByLoaneeIndexAsc(accountNumber.trim());
    }

    public List<BLRS_Guarantor_Entity> getGuarantorsByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return guarantorRepo.findByAccountNumberOrderByGuarantorIdAsc(accountNumber.trim());
    }

    @Transactional
    public BLRS_LoanAccountProfile_Entity saveLoanProfile(BLRS_LoanAccountProfile_Entity entity) {
        return blrsLoanAccountProfileRepo.save(entity);
    }
}

