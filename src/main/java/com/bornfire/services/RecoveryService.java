package com.bornfire.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_LoanAccountProfile_Entity;
import com.bornfire.entities.BLRS_LoanAccountProfile_Repo;
import com.bornfire.entities.BLRS_Recovery_Entity;
import com.bornfire.entities.BLRS_Recovery_Repo;

/**
 * Service managing loan recovery transactions and repayments.
 */
@Service
@Transactional
public class RecoveryService {

    private static final Logger log = LoggerFactory.getLogger(RecoveryService.class);

    @Autowired
    private BLRS_Recovery_Repo recoveryRepo;

    @Autowired
    private BLRS_LoanAccountProfile_Repo loanRepo;

    @Autowired
    private OverdueService overdueService;

    public BLRS_Recovery_Entity recordRecovery(String accountNumber, BigDecimal amount, Date recoveryDate) {
        if (accountNumber == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid recovery record attempt for account {}", accountNumber);
            return null;
        }

        BLRS_LoanAccountProfile_Entity loan = loanRepo.findByAccountNumber(accountNumber.trim());
        if (loan == null) {
            loan = loanRepo.findFirstByAccountNumber(accountNumber.trim());
        }

        Date recDate = recoveryDate != null ? recoveryDate : new Date();

        BigDecimal currentOutstanding = (loan != null && loan.getLoanOutstandingBalance() != null)
                ? loan.getLoanOutstandingBalance()
                : BigDecimal.ZERO;

        BigDecimal newOutstanding = currentOutstanding.subtract(amount);
        BigDecimal currentOverdue = (loan != null && loan.getTotalArrears() != null)
                ? loan.getTotalArrears()
                : BigDecimal.ZERO;

        BigDecimal overdueAfter = currentOverdue.subtract(amount);

        BLRS_Recovery_Entity r = new BLRS_Recovery_Entity();
        r.setRecoveryId("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        r.setAccountNumber(accountNumber.trim());
        r.setRecordDate(recDate);
        r.setRecoveryDate(recDate);
        r.setAmount(amount);
        r.setLoanOutstanding(newOutstanding);
        r.setPrincipal(amount);
        r.setInterest(BigDecimal.ZERO);
        r.setTotal(amount);
        r.setOverdueAfter(overdueAfter);
        r.setStatus(overdueAfter.compareTo(BigDecimal.ZERO) <= 0 ? "RECOVERED" : "PARTIAL");

        if (loan != null) {
            loan.setLoanOutstandingBalance(newOutstanding);
            loan.setTotalArrears(overdueAfter);
            loan.setPaymentsReceived(
                    (loan.getPaymentsReceived() != null ? loan.getPaymentsReceived() : BigDecimal.ZERO).add(amount)
            );
            loan.setRecoveryDate(java.time.LocalDate.now());
            loan.setRecoveryAmount(amount);
            loan.setStatus(overdueService.determineStatus(overdueAfter));
            loanRepo.save(loan);
        }

        return recoveryRepo.save(r);
    }

    public List<BLRS_Recovery_Entity> getRecoveriesForAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return recoveryRepo.findByAccountNumberOrderByRecoveryDateDesc(accountNumber.trim());
    }

    public List<BLRS_Recovery_Entity> getAllRecoveries() {
        return recoveryRepo.findAll();
    }
}
