package com.BillardManagement.Repository;
import com.BillardManagement.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepo extends JpaRepository<Employee, Long> {
    Optional<Object> findById(Employee employeeID);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.clubID.customerID = :customerId")
    Long countByCustomerId(@Param("customerId") Integer customerId);


}

