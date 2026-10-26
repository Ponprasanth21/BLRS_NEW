package com.bornfire.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BLRS_NotificationLog_Repo
        extends JpaRepository<BLRS_NotificationLog_Entity, Long> {

    List<BLRS_NotificationLog_Entity> findByAccountNoOrderBySentTimeDesc(String accountNo);
    List<BLRS_NotificationLog_Entity> findByReminderNoAndStatus(String reminderNo, String status);
    boolean existsByChannelAndAccountNoAndReminderNoAndStatus(String channel, String accountNo, String reminderNo, String status);
}