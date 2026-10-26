package com.bornfire.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_BatchJobExecution_Repo extends JpaRepository<BLRS_BatchJobExecution_Entity, String> {

    List<BLRS_BatchJobExecution_Entity> findByJobIdOrderByStartTimeDesc(String jobId);

    @Query("SELECT e FROM BLRS_BatchJobExecution_Entity e WHERE e.jobId = :jobId AND e.runDate = :runDate AND e.status = :status")
    List<BLRS_BatchJobExecution_Entity> findByJobIdAndRunDateAndStatus(
            @Param("jobId") String jobId,
            @Param("runDate") Date runDate,
            @Param("status") String status);

    @Query("SELECT COUNT(e) > 0 FROM BLRS_BatchJobExecution_Entity e WHERE e.jobId = :jobId AND e.runDate = :runDate AND e.status = 'COMPLETED'")
    boolean isJobCompletedOnDate(@Param("jobId") String jobId, @Param("runDate") Date runDate);

    @Query("SELECT e FROM BLRS_BatchJobExecution_Entity e ORDER BY e.startTime DESC")
    List<BLRS_BatchJobExecution_Entity> findAllRecentExecutions();
}
