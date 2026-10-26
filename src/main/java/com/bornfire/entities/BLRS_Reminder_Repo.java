package com.bornfire.entities;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_Reminder_Repo extends JpaRepository<BLRS_Reminder_Entity, String> {

    boolean existsByAccountNumberAndReminderNo(String accountNumber, String reminderNo);

    Optional<BLRS_Reminder_Entity> findByAccountNumberAndReminderNo(String accountNumber, String reminderNo);

    List<BLRS_Reminder_Entity> findByAccountNumberOrderByReminderDateAsc(String accountNumber);

    @Query("SELECT r FROM BLRS_Reminder_Entity r WHERE r.reminderNo = :reminderNo AND r.reminderDate = :reminderDate")
    List<BLRS_Reminder_Entity> findByReminderNoAndReminderDate(
            @Param("reminderNo") String reminderNo,
            @Param("reminderDate") Date reminderDate);

    @Query("SELECT r.reminderNo FROM BLRS_Reminder_Entity r WHERE r.accountNumber = :accountNumber")
    List<String> findIssuedReminderTypesForAccount(@Param("accountNumber") String accountNumber);
}
