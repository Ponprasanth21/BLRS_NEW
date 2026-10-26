package com.bornfire.services;

import java.io.Serializable;
import java.util.Date;

/**
 * Service interface for Finacle core banking extraction and DRS replication.
 * Handles FIN001 (Daily core banking extraction) and DRS002 (DRS replication).
 *
 * Ground Rule 1: Finacle DB is a SEPARATE datasource, READ-ONLY.
 * Uses JdbcTemplate / native queries / DTO projections. Never touched by JPA ddl-auto.
 */
public interface FinacleExtractionService {

    /**
     * FIN001: Daily Finacle data extraction.
     * Extracts account master, loan master, demand/arrears, repayment tables into
     * LoanAccount, OverdueAccount, and Recovery.
     *
     * @param runDate extraction run date
     * @param runBy username or SYSTEM
     * @return ExtractionResult with processing summary and metrics
     */
    ExtractionResult executeFIN001(Date runDate, String runBy);

    /**
     * DRS002: Disaster Recovery System replication.
     * Replicates from DR / replica DB.
     *
     * @param runDate replication run date
     * @param runBy username or SYSTEM
     * @return ExtractionResult with replication status
     */
    ExtractionResult executeDRS002(Date runDate, String runBy);

    /**
     * Result DTO encapsulating batch data extraction metrics.
     */
    class ExtractionResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private final boolean success;
        private final String jobId;
        private final String message;
        private final int totalRecords;
        private final int successCount;
        private final int failureCount;
        private final String statusFilePath;

        public ExtractionResult(boolean success, String jobId, String message,
                                int totalRecords, int successCount, int failureCount, String statusFilePath) {
            this.success = success;
            this.jobId = jobId;
            this.message = message;
            this.totalRecords = totalRecords;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.statusFilePath = statusFilePath;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getJobId() {
            return jobId;
        }

        public String getMessage() {
            return message;
        }

        public int getTotalRecords() {
            return totalRecords;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public String getStatusFilePath() {
            return statusFilePath;
        }
    }
}
