package com.bornfire.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_DepartmentMaster_Repo extends JpaRepository<BLRS_DepartmentMaster_Entity, String> {

    @Query("SELECT d.departmentCode FROM BLRS_DepartmentMaster_Entity d WHERE d.status = 'ACTIVE' ORDER BY d.departmentCode ASC")
    List<String> findAllActiveDepartmentCodes();

    List<BLRS_DepartmentMaster_Entity> findByStatusOrderByDepartmentCodeAsc(String status);
}
