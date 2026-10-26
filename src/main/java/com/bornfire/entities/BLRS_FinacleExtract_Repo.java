package com.bornfire.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BLRS_FinacleExtract_Repo extends JpaRepository<BLRS_FinacleExtract_Entity, String> {

    @Query("SELECT f FROM BLRS_FinacleExtract_Entity f WHERE " +
           "(LOWER(f.user_id) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR LOWER(f.user_name) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR LOWER(f.emp_id) LIKE LOWER(CONCAT('%', :term, '%'))) " +
           "AND (f.status IS NULL OR f.status = 'ACTIVE') " +
           "ORDER BY f.user_id ASC")
    List<BLRS_FinacleExtract_Entity> searchUsers(@Param("term") String term);

    @Query("SELECT f FROM BLRS_FinacleExtract_Entity f WHERE (f.status IS NULL OR f.status = 'ACTIVE') ORDER BY f.user_id ASC")
    List<BLRS_FinacleExtract_Entity> findAllActive();
}
