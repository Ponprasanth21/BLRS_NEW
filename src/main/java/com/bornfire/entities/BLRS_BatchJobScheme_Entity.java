package com.bornfire.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Batch Job Scheme child table (jobId, schemeCode) - normalizes multi-scheme batch jobs.
 */
@Entity
@Table(name = "BLRS_BATCH_JOB_SCHEME_TAB")
@IdClass(BLRS_BatchJobScheme_Entity.PK.class)
public class BLRS_BatchJobScheme_Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "JOB_ID", length = 50)
    private String jobId;

    @Id
    @Column(name = "SCHEME_CODE", length = 50)
    private String schemeCode;

    @Column(name = "CREATED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime = new Date();

    public static class PK implements Serializable {
        private static final long serialVersionUID = 1L;
        private String jobId;
        private String schemeCode;

        public PK() {
        }

        public PK(String jobId, String schemeCode) {
            this.jobId = jobId;
            this.schemeCode = schemeCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK)) return false;
            PK pk = (PK) o;
            return Objects.equals(jobId, pk.jobId) &&
                    Objects.equals(schemeCode, pk.schemeCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jobId, schemeCode);
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getSchemeCode() {
            return schemeCode;
        }

        public void setSchemeCode(String schemeCode) {
            this.schemeCode = schemeCode;
        }
    }

    public BLRS_BatchJobScheme_Entity() {
    }

    public BLRS_BatchJobScheme_Entity(String jobId, String schemeCode) {
        this.jobId = jobId;
        this.schemeCode = schemeCode;
        this.createdTime = new Date();
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(String schemeCode) {
        this.schemeCode = schemeCode;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
