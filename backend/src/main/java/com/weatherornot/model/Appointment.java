package com.weatherornot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    private LocalDate date;
    private boolean recurring;

    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(name = "appointment_recurrence_days", joinColumns = @JoinColumn(name = "appointment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private Set<DayOfWeek> recurringDays = EnumSet.noneOf(DayOfWeek.class);

    @Embedded
    private Place destination;
}
