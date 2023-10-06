package com.sita.sita.dto;

import com.sita.sita.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BonusResponse {
    private String currency;
    private List<Employee> employees;
}
