
package com.bornfire.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BLRS_LoanAccountProfile_Repo
        extends JpaRepository<BLRS_LoanAccountProfile_Entity, String> {

    // Find loan details using customer ID
    BLRS_LoanAccountProfile_Entity findByCustomerId(String customerId);

}
