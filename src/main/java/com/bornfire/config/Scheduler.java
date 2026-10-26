package com.bornfire.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bornfire.entities.BLRS_BatchJobScheduler_Entity;
import com.bornfire.services.BLRS_BatchJobSchedulerService;

/**
 * Spring Scheduler – polls active verified batch jobs due today and executes them.
 *
 * Cron: every minute ("0 * * * * *").
 * Enabled by @EnableScheduling on BLRSApplication.java.
 *
 * Execution rules:
 * - DATA JOBS (FIN001 / DRS002): run unconditionally when due.
 * - BUSINESS JOBS: subject to Rule 6 (same-day FIN001 must be COMPLETED first).
 */
@Component
public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
    private static final String SYSTEM_USER = "SCHEDULER";

    @Autowired
    private BLRS_BatchJobSchedulerService batchJobService;

    /**
     * Runs every minute and dispatches any verified ACTIVE batch job
     * whose NEXT_RUN_DATE has arrived or passed.
     */
    @Scheduled(cron = "0 * * * * *")
    public void dispatchDueJobs() {
        List<BLRS_BatchJobScheduler_Entity> dueJobs = batchJobService.getAlertList();

        if (dueJobs == null || dueJobs.isEmpty()) {
            return; // nothing due
        }

        log.info("Scheduler: {} job(s) due for automatic execution.", dueJobs.size());

        for (BLRS_BatchJobScheduler_Entity job : dueJobs) {
            try {
                log.info("Scheduler: dispatching job {} ({})", job.getJob_id(), job.getJob_name());
                String result = batchJobService.runJob(job.getJob_id(), SYSTEM_USER);
                log.info("Scheduler: job {} result -> {}", job.getJob_id(), result);
            } catch (Exception e) {
                log.error("Scheduler: unexpected error running job {}", job.getJob_id(), e);
            }
        }
    }
}
