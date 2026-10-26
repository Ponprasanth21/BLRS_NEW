
package com.bornfire.entities;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_LoanAccountProfile_Repo
        extends JpaRepository<BLRS_LoanAccountProfile_Entity, String> {

    // Find loan details using customer ID
    BLRS_LoanAccountProfile_Entity findByCustomerId(String customerId);

    // Find loan details using account number fallback
    BLRS_LoanAccountProfile_Entity findFirstByAccountNumber(String accountNumber);

    BLRS_LoanAccountProfile_Entity findByAccountNumber(String accountNumber);

    
    
    
 // =====================================================================
 // BLRS_LoanAccountProfile_Repo.java  -  ADD these 4 methods inside the interface
 // (keep your existing  findByCustomerId(...)  as it is)
 // Reports read the SAME table as Loan Account Profile : blrs_loan_account_profile_tab
 // Imports needed at the top :  java.time.LocalDate, java.util.List,
//                               org.springframework.data.jpa.repository.Query
 // =====================================================================

 	// ---- Overdue report : month filter on OVERDUE_RECORD_DATE ----
 	List<BLRS_LoanAccountProfile_Entity> findByOverdueRecordDateGreaterThanEqualAndOverdueRecordDateLessThanOrderBySchemeAscCustomerIdAsc(
 			LocalDate from, LocalDate to);

 	// ---- Reminder + Guarantor report : month filter on REMINDER_DATE ----
 	List<BLRS_LoanAccountProfile_Entity> findByReminderDateGreaterThanEqualAndReminderDateLessThanOrderByReminderDateAscCustomerIdAsc(
 			LocalDate from, LocalDate to);

 	// ---- drop-downs on the report screens ----
 	@Query("SELECT DISTINCT r.scheme FROM BLRS_LoanAccountProfile_Entity r WHERE r.scheme IS NOT NULL ORDER BY r.scheme")
 	List<String> getSchemeCodes();

 	@Query("SELECT DISTINCT r.overdueDepartment FROM BLRS_LoanAccountProfile_Entity r WHERE r.overdueDepartment IS NOT NULL ORDER BY r.overdueDepartment")
 	List<String> getDepartments();
}
