package com.sita.sita.controller;

import com.sita.sita.dto.BonusResponse;
import com.sita.sita.model.Department;
import com.sita.sita.model.Employee;
import com.sita.sita.repository.DepartmentRepository;
import com.sita.sita.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/tci/employee-bonus")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @PostMapping
    public ResponseEntity<String> storeEmployeeData(@RequestBody List<Employee> employees) {
        for (Employee employee : employees) {
            // Perform a lookup to find the Department entity based on departmentName
            Department department = departmentRepository.findByDepartmentName(employee.getDepartment());

            // If the department does not exist, create a new one
            if (department == null) {
                department = new Department();
                department.setDepartmentName(employee.getDepartment());
                departmentRepository.save(department);
            }

            // Associate the Department with the Employee
            employee.setDepartment(department.getDepartmentName());
        }

        // Save employees to the database
        employeeRepository.saveAll(employees);
        return ResponseEntity.ok("Employee data saved successfully.");
    }

    @GetMapping
    public ResponseEntity<?> getEligibleEmployees(
            @RequestParam("date") @DateTimeFormat(pattern = "MMM-dd-yyyy") LocalDate date) {
        try {
            List<Employee> allEmployees = employeeRepository.findAll();

            // Filter employees who are active on the requested date
            List<Employee> eligibleEmployees = allEmployees.stream()
                    .filter(emp -> emp.getJoiningDate().isBefore(date) && (emp.getExitDate() == null || emp.getExitDate().isAfter(date)))
                    .collect(Collectors.toList());

            if (eligibleEmployees.isEmpty()) {
                // Return a custom response for no eligible employees found
                Map<String, Object> response = new HashMap<>();
                response.put("errorMessage", "No eligible employees found for the given date.");
                response.put("data", Collections.emptyList());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Group eligible employees by currency
            Map<String, List<Employee>> employeesByCurrency = eligibleEmployees.stream()
                    .collect(Collectors.groupingBy(Employee::getCurrency));

            // Sort employees by name within each currency group
            employeesByCurrency.forEach((currency, employees) -> employees.sort(Comparator.comparing(Employee::getEmpName)));

            // Prepare the response in the desired format
            List<Map<String, Object>> responseData = new ArrayList<>();
            for (Map.Entry<String, List<Employee>> entry : employeesByCurrency.entrySet()) {
                Map<String, Object> currencyData = new HashMap<>();
                currencyData.put("currency", entry.getKey());

                List<Map<String, Object>> employeesList = new ArrayList<>();
                for (Employee emp : entry.getValue()) {
                    Map<String, Object> employeeData = new HashMap<>();
                    employeeData.put("empName", emp.getEmpName());
                    employeeData.put("amount", emp.getAmount());
                    employeesList.add(employeeData);
                }

                currencyData.put("employees", employeesList);
                responseData.add(currencyData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("errorMessage", "");
            response.put("data", responseData);
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            // Handle the date parsing error
            return ResponseEntity.badRequest().body("Invalid date format. Please provide a date in MMM-dd-yyyy format.");
        } catch (DataAccessException e) {
            // Handle the database query error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving data from the database.");
        } catch (Exception e) {
            // Handle other runtime exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


}

