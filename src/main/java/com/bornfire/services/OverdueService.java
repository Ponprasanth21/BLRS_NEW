package com.bornfire.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_LoanAccountProfile_Entity;
import com.bornfire.entities.BLRS_LoanAccountProfile_Repo;
import com.bornfire.entities.BLRS_OverdueAccount_Entity;
import com.bornfire.entities.BLRS_OverdueAccount_Repo;

/**
 * Service calculating loan overdues, age, and payment status per scheme.
 *
 * Ground Rule 7:
 * - Use BigDecimal for all money.
 * - Overdue can be negative (excess payment); if overdue <= 0 then status = PAID.
 * - If overdue > 0 then status = OVERDUE.
 */
@Service
@Transactional
public class OverdueService {

    private static final Logger log = LoggerFactory.getLogger(OverdueService.class);

    @Autowired
    private BLRS_OverdueAccount_Repo overdueRepo;

    @Autowired
    private BLRS_LoanAccountProfile_Repo loanAccountRepo;

    /**
     * Determines payment status based on overdue amount.
     * Rule 7: Overdue can be negative (excess payment); if overdue <= 0 then status = PAID.
     */
    public String determineStatus(BigDecimal overdueAmount) {
        if (overdueAmount == null || overdueAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return "PAID";
        }
        return "OVERDUE";
    }

    /**
     * Computes overdue amount:
     * Overdue = capitalArrears + interestArrears (or totalArrears).
     * If neither is present, computes from (outstandingBalance - original/scheduled balance).
     */
    public BigDecimal calculateOverdueAmount(BigDecimal capitalArrears, BigDecimal interestArrears, BigDecimal totalArrears) {
        if (totalArrears != null) {
            return totalArrears.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal cap = capitalArrears != null ? capitalArrears : BigDecimal.ZERO;
        BigDecimal intr = interestArrears != null ? interestArrears : BigDecimal.ZERO;
        return cap.add(intr).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates age in days from due date to record date.
     */
    public int calculateAgeInDays(Date dueDate, Date recordDate) {
        if (dueDate == null || recordDate == null) {
            return 0;
        }
        long diffMillis = recordDate.getTime() - dueDate.getTime();
        if (diffMillis <= 0) {
            return 0;
        }
        return (int) (diffMillis / (1000L * 60 * 60 * 24));
    }

    /**
     * Creates and saves an Overdue Account snapshot for a given loan profile and record date.
     */
    public BLRS_OverdueAccount_Entity computeAndSaveOverdue(BLRS_LoanAccountProfile_Entity loan, Date recordDate) {
        if (loan == null || loan.getAccountNumber() == null) {
            return null;
        }
        Date cleanDate = truncateDate(recordDate != null ? recordDate : new Date());

        BigDecimal capitalArrears = loan.getLoanCapitalArrears() != null
                ? loan.getLoanCapitalArrears()
                : (loan.getOverdueCapitalArrears() != null ? loan.getOverdueCapitalArrears() : BigDecimal.ZERO);

        BigDecimal interestArrears = loan.getLoanInterestArrears() != null
                ? loan.getLoanInterestArrears()
                : (loan.getOverdueInterestArrears() != null ? loan.getOverdueInterestArrears() : BigDecimal.ZERO);

        BigDecimal overdueAmount = calculateOverdueAmount(capitalArrears, interestArrears, loan.getTotalArrears());
        String status = determineStatus(overdueAmount);

        Date dueDate = null;
        if (loan.getLoanDisbursementDate() != null) {
            dueDate = Date.from(loan.getLoanDisbursementDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        int ageDays = calculateAgeInDays(dueDate, cleanDate);
        int monthArrears = loan.getNumberOfMonthsInArrears() != null
                ? loan.getNumberOfMonthsInArrears()
                : (loan.getOverdueMonthArrears() != null ? loan.getOverdueMonthArrears() : (ageDays / 30));

        BLRS_OverdueAccount_Entity entity = new BLRS_OverdueAccount_Entity();
        entity.setAccountNumber(loan.getAccountNumber().trim());
        entity.setRecordDate(cleanDate);
        entity.setCustomerId(loan.getCustomerId());
        entity.setBorrowerName(loan.getBorrowerName());
        entity.setSchemeCode(loan.getScheme());
        entity.setDepartment(loan.getOverdueDepartment());
        entity.setLoanAmount(loan.getLoanAmountApplied() != null ? loan.getLoanAmountApplied() : BigDecimal.ZERO);
        entity.setOutstandingBalance(loan.getLoanOutstandingBalance() != null ? loan.getLoanOutstandingBalance() : BigDecimal.ZERO);
        entity.setInterestRate(loan.getActualInterestRate() != null ? loan.getActualInterestRate() : BigDecimal.ZERO);
        entity.setTotalArrears(overdueAmount);
        entity.setCapitalArrears(capitalArrears);
        entity.setInterestArrears(interestArrears);
        entity.setMonthArrears(monthArrears);
        entity.setOverdueAmount(overdueAmount);
        entity.setAgeDays(ageDays);
        entity.setDueDate(dueDate);
        entity.setStatus(status);

        // Also update the loan profile's active status and age
        loan.setStatus(status);
        loan.setAge(ageDays);
        loan.setOverdueMonthArrears(monthArrears);
        loan.setOverdueLoanAmount(overdueAmount);
        loanAccountRepo.save(loan);

        return overdueRepo.save(entity);
    }

    /**
     * Computes overdue snapshots for all loan accounts matching a scheme for the specified date.
     */
    public int computeAllOverdues(String schemeCode, String department, Date recordDate) {
        List<BLRS_LoanAccountProfile_Entity> loans = loanAccountRepo.findAll();
        int count = 0;
        for (BLRS_LoanAccountProfile_Entity loan : loans) {
            if (schemeCode != null && !schemeCode.trim().isEmpty() && !"ALL".equalsIgnoreCase(schemeCode.trim())) {
                if (loan.getScheme() == null || !schemeCode.trim().equalsIgnoreCase(loan.getScheme().trim())) {
                    continue;
                }
            }
            if (department != null && !department.trim().isEmpty() && !"ALL".equalsIgnoreCase(department.trim())) {
                if (loan.getOverdueDepartment() == null || !department.trim().equalsIgnoreCase(loan.getOverdueDepartment().trim())) {
                    continue;
                }
            }
            computeAndSaveOverdue(loan, recordDate);
            count++;
        }
        return count;
    }

    private Date truncateDate(Date d) {
        if (d == null) return null;
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(d);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
