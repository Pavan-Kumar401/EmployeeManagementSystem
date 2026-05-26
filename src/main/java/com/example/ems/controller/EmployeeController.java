package com.example.ems.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ems.entity.Employee;
import com.example.ems.entity.LeaveRequest;
import com.example.ems.repository.LeaveRequestRepository;
import com.example.ems.service.EmployeeService;
import com.example.ems.service.LeaveRequestService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveRequestService leaveRequestService;
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;


    // Employee Login Page
    @GetMapping("/login")
    public String employeeLoginPage() {
        return "employee-login";
    }

    @GetMapping("/portal")
    public String employeePortal() {
        return "employee-login";
    }
    
    // Employee Login
    @PostMapping("/login")
    public String loginEmployee(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Employee employee =
                employeeService.login(username, password);

        if (employee != null) {

            session.setAttribute("employee", employee);

            return "redirect:/employee/dashboard";
        }

        model.addAttribute(
                "error",
                "Invalid username or password");

        return "employee-login";
    }


    // Employee Register Page
    @GetMapping("/register")
    public String employeeRegisterPage() {
        return "employee-register";
    }


    // Employee Registration
    @PostMapping("/register")
    public String registerEmployee(
            @RequestParam String employeeCode,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        Employee employee =
                employeeService.getEmployeeByCode(employeeCode);

        if (employee == null) {
            model.addAttribute(
                    "error",
                    "Employee code not found");
            return "employee-register";
        }

        if (!employee.getEmail().equalsIgnoreCase(email)) {
            model.addAttribute(
                    "error",
                    "Email does not match employee records");
            return "employee-register";
        }

        if (employeeService.getEmployeeByUsername(username) != null) {
            model.addAttribute(
                    "error",
                    "Username already exists");
            return "employee-register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute(
                    "error",
                    "Passwords do not match");
            return "employee-register";
        }

        employeeService.registerEmployee(
                employeeCode,
                email,
                username,
                password);

        model.addAttribute(
                "success",
                "Registration successful. Please sign in.");

        return "employee-login";
    }


    // Forgot Password Page
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }


    // Reset Password
    @PostMapping("/forgot-password")
    public String resetPassword(
            @RequestParam String employeeCode,
            @RequestParam String email,
            @RequestParam String newPassword,
            Model model) {

        Employee employee =
                employeeService.getEmployeeByCode(employeeCode);

        if (employee == null) {
            model.addAttribute(
                    "error",
                    "Employee code not found");
            return "forgot-password";
        }

        if (!employee.getEmail().equalsIgnoreCase(email)) {
            model.addAttribute(
                    "error",
                    "Email does not match our records");
            return "forgot-password";
        }

        employee.setPassword(newPassword);

        employeeService.updateEmployee(employee);

        model.addAttribute(
                "success",
                "Password updated successfully. Please login.");

        return "employee-login";
    }


    // Dashboard
    @GetMapping("/dashboard")
    public String employeeDashboard(
            HttpSession session,
            Model model) {

        Employee employee =
                (Employee) session.getAttribute("employee");

        if (employee == null) {
            return "redirect:/employee/login";
        }

        model.addAttribute("employee", employee);

        return "employee-dashboard";
    }


    // Profile
    @GetMapping("/profile")
    public String employeeProfile(
            HttpSession session,
            Model model) {

        Employee employee =
                (Employee) session.getAttribute("employee");

        if (employee == null) {
            return "redirect:/employee/login";
        }

        model.addAttribute("employee", employee);

        return "employee-profile";
    }


    // Edit Profile Page
    @GetMapping("/edit")
    public String editProfilePage(
            HttpSession session,
            Model model) {

        Employee employee =
                (Employee) session.getAttribute("employee");

        if (employee == null) {
            return "redirect:/employee/login";
        }

        model.addAttribute("employee", employee);

        return "edit-profile";
    }


    // Update Profile
    @PostMapping("/update-profile")
    public String updateProfile(
            @ModelAttribute Employee employee,
            HttpSession session,
            Model model) {

        Employee existingEmployee =
                employeeService.getEmployeeById(employee.getId());

        existingEmployee.setMobile(employee.getMobile());
        existingEmployee.setAddress(employee.getAddress());

        employeeService.updateEmployee(existingEmployee);

        session.setAttribute("employee", existingEmployee);

        model.addAttribute("employee", existingEmployee);

        return "employee-profile";
    }


    // Apply Leave Page
    @GetMapping("/apply-leave")
    public String applyLeavePage(
            HttpSession session,
            Model model) {

        Employee employee =
                (Employee) session.getAttribute("employee");

        if (employee == null) {
            return "redirect:/employee/login";
        }

        List<LeaveRequest> leaveRequests =
                leaveRequestRepository.findByEmployeeCode(
                        employee.getEmployeeCode()
                );

        // 1. check pending request
        boolean hasPendingLeave =
                leaveRequests.stream()
                        .anyMatch(leave ->
                                "Pending".equalsIgnoreCase(
                                        leave.getStatus()
                                ));

        // 2. check approved leave count this month
        LocalDate today = LocalDate.now();

        long approvedLeaveCount =
                leaveRequests.stream()
                        .filter(leave ->
                                leave.getActionDate() != null
                                && "Approved".equalsIgnoreCase(
                                        leave.getStatus())
                                && leave.getActionDate().getMonthValue()
                                        == today.getMonthValue()
                                && leave.getActionDate().getYear()
                                        == today.getYear()
                        )
                        .count();

        boolean monthlyLeaveLimitReached =
                approvedLeaveCount >= 2;

        model.addAttribute(
                "hasPendingLeave",
                hasPendingLeave);

        model.addAttribute(
                "monthlyLeaveLimitReached",
                monthlyLeaveLimitReached);

        return "apply-leave";
    }


    // Submit Leave Request
    @PostMapping("/apply-leave")
    public String submitLeaveRequest(
            @RequestParam String leaveType,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String reason,
            HttpSession session,
            Model model) {

        Employee employee =
                (Employee) session.getAttribute("employee");

        if (employee == null) {
            return "redirect:/employee/login";
        }

        LocalDate startDate = LocalDate.parse(fromDate);
        LocalDate endDate = LocalDate.parse(toDate);
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today)) {
            model.addAttribute(
                    "error",
                    "Leave start date cannot be in the past.");
            return "apply-leave";
        }

        if (endDate.isBefore(startDate)) {
            model.addAttribute(
                    "error",
                    "End date cannot be before start date.");
            return "apply-leave";
        }

        if (leaveRequestService.hasPendingLeaveRequest(
                employee.getEmployeeCode())) {

            model.addAttribute(
                    "error",
                    "You already have a pending leave request.");

            return "apply-leave";
        }

        LeaveRequest leaveRequest = new LeaveRequest();

        leaveRequest.setEmployeeCode(employee.getEmployeeCode());

        leaveRequest.setEmployeeName(
                employee.getFirstName() + " " + employee.getLastName());

        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setFromDate(startDate);
        leaveRequest.setToDate(endDate);
        leaveRequest.setReason(reason);
        leaveRequest.setStatus("Pending");

        leaveRequestService.saveLeaveRequest(leaveRequest);

        model.addAttribute(
                "success",
                "Leave request submitted successfully.");

        return "apply-leave";
    }


    // My Leave Status
    @GetMapping("/my-leaves")
    public String myLeaves(
            HttpSession session,
            Model model) {

        Employee employee =
                (Employee) session.getAttribute("employee");

        if (employee == null) {
            return "redirect:/employee/login";
        }

        List<LeaveRequest> leaveRequests =
                leaveRequestService
                        .getLeaveRequestsByEmployeeCode(
                                employee.getEmployeeCode());

        model.addAttribute(
                "leaveRequests",
                leaveRequests);

        return "employee-leave-status";
    }


    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/employee/login";
    }
}