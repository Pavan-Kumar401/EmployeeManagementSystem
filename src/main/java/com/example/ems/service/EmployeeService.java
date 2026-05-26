package com.example.ems.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ems.entity.Employee;
import com.example.ems.repository.EmployeeRepository;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee saveEmployee(Employee employee) {

        if(employee.getEmployeeCode() == null ||
           employee.getEmployeeCode().isEmpty()) {

            Employee lastEmployee =
                    employeeRepository.findTopByOrderByIdDesc();

            int nextNumber = 1;

            if(lastEmployee != null &&
               lastEmployee.getEmployeeCode() != null) {

                String code =
                        lastEmployee.getEmployeeCode()
                                    .replace("EMP", "");

                nextNumber =
                        Integer.parseInt(code) + 1;
            }

            employee.setEmployeeCode(
                    "EMP" + String.format("%03d", nextNumber)
            );
        }

        return employeeRepository.save(employee);
    }


    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }


    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }


    public List<Employee> searchByFirstName(String firstName) {
        return employeeRepository
                .findByFirstNameContainingIgnoreCase(firstName);
    }


    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository
                .findByDepartmentIgnoreCase(department);
    }


    public Employee updateEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }


    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }


    public Employee getEmployeeByCode(String employeeCode) {
        return employeeRepository
                .findByEmployeeCode(employeeCode)
                .orElse(null);
    }


    public Employee getEmployeeByUsername(String username) {
        return employeeRepository
                .findByUsername(username)
                .orElse(null);
    }


    public void registerEmployee(
            String employeeCode,
            String email,
            String username,
            String password) {

        Employee employee =
                getEmployeeByCode(employeeCode);

        if(employee != null) {

            employee.setUsername(username);
            employee.setPassword(password);

            employeeRepository.save(employee);
        }
    }


    /*
     * LOGIN METHOD
     * ADD THIS
     */
    public Employee login(
            String username,
            String password) {

        Employee employee =
                getEmployeeByUsername(username);

        if(employee != null &&
           employee.getPassword() != null &&
           employee.getPassword().equals(password)) {

            return employee;
        }

        return null;
    }

}