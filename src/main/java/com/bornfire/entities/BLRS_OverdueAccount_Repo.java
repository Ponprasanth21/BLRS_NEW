package com.bornfire.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_OverdueAccount_Repo
        extends JpaRepository<BLRS_OverdueAccount_Entity, BLRS_OverdueAccount_Entity.PK> {

    List<BLRS_OverdueAccount_Entity> findByRecordDate(Date recordDate);

    List<BLRS_OverdueAccount_Entity> findByAccountNumberOrderByRecordDateDesc(String accountNumber);

    @Query("SELECT o FROM BLRS_OverdueAccount_Entity o WHERE o.recordDate = :recordDate AND (:schemeCode IS NULL OR :schemeCode = '' OR :schemeCode = 'ALL' OR o.schemeCode = :schemeCode) AND (:department IS NULL OR :department = '' OR :department = 'ALL' OR o.department = :department) AND o.status = 'OVERDUE' ORDER BY o.schemeCode, o.accountNumber")
    List<BLRS_OverdueAccount_Entity> findOverdueForReminder(
            @Param("recordDate") Date recordDate,
            @Param("schemeCode") String schemeCode,
            @Param("department") String department);
}
