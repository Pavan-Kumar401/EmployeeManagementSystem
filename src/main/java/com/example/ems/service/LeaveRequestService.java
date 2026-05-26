package com.example.ems.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ems.entity.LeaveRequest;
import com.example.ems.repository.LeaveRequestRepository;

@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    public LeaveRequest saveLeaveRequest(
            LeaveRequest leaveRequest) {

        return leaveRequestRepository.save(leaveRequest);
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getLeaveRequestsByEmployeeCode(
            String employeeCode) {

        return leaveRequestRepository
                .findByEmployeeCode(employeeCode);
    }

    public LeaveRequest getLeaveRequestById(Long id) {

        return leaveRequestRepository
                .findById(id)
                .orElse(null);
    }

    public LeaveRequest updateLeaveStatus(
            LeaveRequest leaveRequest) {

        return leaveRequestRepository.save(leaveRequest);
    }
    
    public boolean hasPendingLeaveRequest(
            String employeeCode) {

        return leaveRequestRepository
                .existsByEmployeeCodeAndStatus(
                        employeeCode,
                        "Pending");
    }
    
    public List<LeaveRequest> getLeaveRequestsByStatus(
            String status) {

        return leaveRequestRepository.findByStatus(status);
    }
    
    public int getMonthlyLeaveCount(String employeeCode) {

        List<LeaveRequest> leaveRequests =
                leaveRequestRepository
                        .findByEmployeeCode(employeeCode);

        LocalDate now = LocalDate.now();

        return (int) leaveRequests.stream()
                .filter(leave ->
                        leave.getFromDate().getMonth() == now.getMonth()
                        &&
                        leave.getFromDate().getYear() == now.getYear())
                .count();
    }
}