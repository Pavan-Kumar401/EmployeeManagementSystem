package com.example.ems.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ems.entity.Employee;
import com.example.ems.entity.LeaveRequest;
import com.example.ems.service.EmployeeService;
import com.example.ems.service.LeaveRequestService;

import jakarta.validation.Valid;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;


    // =========================
    // ADMIN LOGIN
    // =========================

    @GetMapping("/login")
    public String loginPage() {
        return "admin-login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        if (adminUsername.equals(username)
                && adminPassword.equals(password)) {

            return "redirect:/admin/dashboard";
        }

        model.addAttribute(
                "error",
                "Incorrect username or password. Please try again.");

        return "admin-login";
    }


    // =========================
    // ADMIN DASHBOARD
    // =========================

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin-dashboard";
    }


    // =========================
    // ADD EMPLOYEE
    // =========================

    @GetMapping("/add-employee")
    public String addEmployeePage(Model model) {
        model.addAttribute("employee", new Employee());
        return "add-employee";
    }

    @PostMapping("/save-employee")
    public String saveEmployee(
            @Valid @ModelAttribute Employee employee,
            BindingResult result) {

        if (result.hasErrors()) {
            return "add-employee";
        }

        employeeService.saveEmployee(employee);

        return "redirect:/admin/dashboard";
    }


    // =========================
    // VIEW EMPLOYEES
    // =========================

    @GetMapping("/view-employees")
    public String viewEmployees(Model model) {

        model.addAttribute(
                "employees",
                employeeService.getAllEmployees());

        return "view-employees";
    }


    // =========================
    // SEARCH EMPLOYEE
    // =========================

    @GetMapping("/search")
    public String searchPage() {
        return "search-employee";
    }

    @PostMapping("/search")
    public String searchEmployee(
            @RequestParam String keyword,
            Model model) {

        try {

            Long id = Long.parseLong(keyword);

            Employee employee =
                    employeeService.getEmployeeById(id);

            if (employee != null) {
                model.addAttribute(
                        "employees",
                        List.of(employee));
            }

        } catch (NumberFormatException e) {

            model.addAttribute(
                    "employees",
                    employeeService.searchByFirstName(keyword));
        }

        return "search-employee";
    }


    // =========================
    // FILTER BY DEPARTMENT
    // =========================

    @GetMapping("/filter")
    public String filterPage() {
        return "filter-employees";
    }

    @PostMapping("/filter")
    public String filterByDepartment(
            @RequestParam String department,
            Model model) {

        model.addAttribute(
                "employees",
                employeeService.getEmployeesByDepartment(department));

        return "filter-employees";
    }


    // =========================
    // UPDATE EMPLOYEE
    // =========================

    @GetMapping("/update")
    public String updateSearchPage() {
        return "update-employee-search";
    }

    @PostMapping("/find-employee")
    public String findEmployeeForUpdate(
            @RequestParam Long id,
            Model model) {

        Employee employee =
                employeeService.getEmployeeById(id);

        if (employee == null) {

            model.addAttribute(
                    "error",
                    "Employee not found");

            return "update-employee-search";
        }

        model.addAttribute("employee", employee);

        return "update-employee";
    }

    @PostMapping("/update-employee")
    public String updateEmployee(
            @ModelAttribute Employee employee) {

        employeeService.updateEmployee(employee);

        return "redirect:/admin/view-employees";
    }


    // =========================
    // DELETE EMPLOYEE
    // =========================

    @GetMapping("/delete")
    public String deletePage() {
        return "delete-employee";
    }

    @PostMapping("/delete-employee")
    public String deleteEmployee(
            @RequestParam Long id,
            Model model) {

        Employee employee =
                employeeService.getEmployeeById(id);

        if (employee == null) {

            model.addAttribute(
                    "error",
                    "Employee not found");

            return "delete-employee";
        }

        employeeService.deleteEmployee(id);

        return "redirect:/admin/view-employees";
    }


    // =========================
    // MANAGE LEAVE REQUESTS
    // =========================

    @GetMapping("/manage-leaves")
    public String manageLeaves(Model model) {

        model.addAttribute(
                "pendingLeaves",
                leaveRequestService.getLeaveRequestsByStatus("Pending"));

        model.addAttribute(
                "approvedLeaves",
                leaveRequestService.getLeaveRequestsByStatus("Approved"));

        model.addAttribute(
                "rejectedLeaves",
                leaveRequestService.getLeaveRequestsByStatus("Rejected"));

        return "manage-leaves";
    }


    // =========================
    // APPROVE LEAVE
    // =========================

    @GetMapping("/approve-leave")
    public String approveLeave(@RequestParam Long id) {

        LeaveRequest leaveRequest =
                leaveRequestService.getLeaveRequestById(id);

        if (leaveRequest != null &&
            "Pending".equals(leaveRequest.getStatus())) {

            leaveRequest.setStatus("Approved");

            leaveRequest.setActionDate(
                    java.time.LocalDate.now());

            leaveRequestService.updateLeaveStatus(leaveRequest);
        }

        return "redirect:/admin/manage-leaves";
    }


    // =========================
    // REJECT LEAVE
    // =========================

    @GetMapping("/reject-leave")
    public String rejectLeave(@RequestParam Long id) {

        LeaveRequest leaveRequest =
                leaveRequestService.getLeaveRequestById(id);

        if (leaveRequest != null &&
            "Pending".equals(leaveRequest.getStatus())) {

            leaveRequest.setStatus("Rejected");

            leaveRequest.setActionDate(
                    java.time.LocalDate.now());

            leaveRequestService.updateLeaveStatus(leaveRequest);
        }

        return "redirect:/admin/manage-leaves";
    }

}