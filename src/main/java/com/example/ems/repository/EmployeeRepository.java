package com.example.ems.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ems.entity.Employee;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    Optional<Employee> findById(Long id);

    List<Employee> findByFirstNameContainingIgnoreCase(String firstName);

    List<Employee> findByDepartmentIgnoreCase(String department);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByUsername(String username);
    
    Employee findTopByOrderByIdDesc();
}