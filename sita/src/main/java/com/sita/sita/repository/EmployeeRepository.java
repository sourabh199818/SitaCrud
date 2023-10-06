package com.sita.sita.repository;

import com.sita.sita.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByExitDateAfter(LocalDate date);

}
