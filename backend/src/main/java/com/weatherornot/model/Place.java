package com.weatherornot.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Place {
    private String displayName;
    private String googlePlaceId;
    private Double latitude;
    private Double longitude;

    public boolean isRoutable() {
        return (googlePlaceId != null && !googlePlaceId.isBlank())
                || (latitude != null && longitude != null);
    }
}
