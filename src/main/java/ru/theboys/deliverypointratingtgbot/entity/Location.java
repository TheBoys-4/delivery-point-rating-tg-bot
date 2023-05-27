package ru.theboys.deliverypointratingtgbot.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.theboys.deliverypointratingtgbot.enums.LocationType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private String administrativeDistrict;

    private String district;

    private String address;

    private String coordinate;

    @Enumerated(EnumType.ORDINAL)
    private LocationType locationType;
}
