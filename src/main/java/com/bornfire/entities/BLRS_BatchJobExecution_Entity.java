package com.bornfire.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Batch Job Execution audit entity tracking each automated or manual run.
 * (executionId, jobId, runDate, startTime, endTime, status STARTED/COMPLETED/FAILED, runBy, statusFilePath)
 */
@Entity
@Table(name = "BLRS_BATCH_JOB_EXECUTION_TAB")
public class BLRS_BatchJobExecution_Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EXECUTION_ID", length = 100)
    private String executionId;

    @Column(name = "JOB_ID", length = 50)
    private String jobId;

    @Column(name = "JOB_NAME", length = 150)
    private String jobName;

    @Column(name = "RUN_DATE")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date runDate;

    @Column(name = "START_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private Date startTime;

    @Column(name = "END_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private Date endTime;

    @Column(name = "STATUS", length = 30)
    private String status; // STARTED, COMPLETED, FAILED

    @Column(name = "RUN_BY", length = 50)
    private String runBy;

    @Column(name = "STATUS_FILE_PATH", length = 300)
    private String statusFilePath;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "TOTAL_RECORDS")
    private Integer totalRecords = 0;

    @Column(name = "SUCCESS_COUNT")
    private Integer successCount = 0;

    @Column(name = "FAILURE_COUNT")
    private Integer failureCount = 0;

    public BLRS_BatchJobExecution_Entity() {
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRunBy() {
        return runBy;
    }

    public void setRunBy(String runBy) {
        this.runBy = runBy;
    }

    public String getStatusFilePath() {
        return statusFilePath;
    }

    public void setStatusFilePath(String statusFilePath) {
        this.statusFilePath = statusFilePath;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }
}
