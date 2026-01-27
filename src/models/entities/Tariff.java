package models.entities;

import java.math.BigDecimal;
import java.time.LocalTime;
import models.util.BaseEntity;

public class Tariff extends BaseEntity {

    private final String name;
    private final BigDecimal pricePerHour;
    private final LocalTime startHour;
    private final LocalTime endHour;
    private final boolean isNightTariff;

    public Tariff(String name, BigDecimal pricePerHour, LocalTime startHour, LocalTime endHour,
          boolean isNightTariff) {
        super();
        this.name = name;
        this.pricePerHour = pricePerHour;
        this.startHour = startHour;
        this.endHour = endHour;
        this.isNightTariff = isNightTariff;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public LocalTime getStartHour() {
        return startHour;
    }

    public LocalTime getEndHour() {
        return endHour;
    }

    public boolean isNightTariff() {
        return isNightTariff;
    }

    @Override
    public String toString() {
        return "Tariff{" +
              "id=" + getId() +
              ",    name='" + name + '\'' +
              ", pricePerHour=" + pricePerHour +
              ", startHour=" + startHour +
              ", endHour=" + endHour +
              ", isNightTariff=" + isNightTariff +
              '}';
    }
}
