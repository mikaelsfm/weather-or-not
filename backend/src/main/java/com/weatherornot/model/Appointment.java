package com.weatherornot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String type;

    @Column(nullable = false)
    public LocalTime startTime;

    public LocalDate date;
    public String locationName;
    public Double latitude;
    public Double longitude;
    public int preparationMinutes;
    public int travelMinutes;
    public int safetyMarginMinutes;
    public boolean recurring;

    // Stored as comma-separated ISO-8601 day numbers (for example, "1,3,5").
    @Column(length = 32)
    public String recurringDays;

    public Set<DayOfWeek> recurrenceDays() {
        if (recurringDays == null || recurringDays.isBlank()) {
            return EnumSet.noneOf(DayOfWeek.class);
        }
        Set<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
        for (String value : recurringDays.split(",")) {
            result.add(DayOfWeek.of(Integer.parseInt(value)));
        }
        return result;
    }
}
