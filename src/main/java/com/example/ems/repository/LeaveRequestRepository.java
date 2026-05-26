package com.example.ems.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.ems.entity.LeaveRequest;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCodeAndStatus(
            String employeeCode,
            String status);

    List<LeaveRequest> findByStatus(String status);
}