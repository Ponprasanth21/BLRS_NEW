package com.bornfire.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_SchemeMaster_Repo extends JpaRepository<BLRS_SchemeMaster_Entity, String> {

    @Query("SELECT s.schemeCode FROM BLRS_SchemeMaster_Entity s WHERE s.status = 'ACTIVE' ORDER BY s.schemeCode ASC")
    List<String> findAllActiveSchemeCodes();

    List<BLRS_SchemeMaster_Entity> findByStatusOrderBySchemeCodeAsc(String status);
}
