package com.bornfire.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BLRS_BatchJobScheme_Repo
        extends JpaRepository<BLRS_BatchJobScheme_Entity, BLRS_BatchJobScheme_Entity.PK> {

    List<BLRS_BatchJobScheme_Entity> findByJobId(String jobId);

    @Query("SELECT s.schemeCode FROM BLRS_BatchJobScheme_Entity s WHERE s.jobId = :jobId ORDER BY s.schemeCode ASC")
    List<String> findSchemeCodesByJobId(@Param("jobId") String jobId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BLRS_BatchJobScheme_Entity s WHERE s.jobId = :jobId")
    void deleteByJobId(@Param("jobId") String jobId);
}
