package com.sita.sita.model;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sita.sita.service.LocalDateDeserializer;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empName;

    private String department; // Change the data type to String

    private String currency;
    private BigDecimal amount;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate joiningDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate exitDate;

}

