package com.weatherornot.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "routine_settings")
@Getter
@Setter
@NoArgsConstructor
public class RoutineSettings {
    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Embedded
    private Place home;

    private int preparationMinutes = 30;
    private int safetyMarginMinutes = 10;
}
