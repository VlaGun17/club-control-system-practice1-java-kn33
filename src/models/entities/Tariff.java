package Entities;

import java.math.BigDecimal;

public class Tariff extends BaseEntity {

    private final String name;
    private final BigDecimal pricePerHour;
    private final int startHour;
    private final int endHour;
    private final boolean isNightTariff;

    public Tariff(String name, BigDecimal pricePerHour, int startHour, int endHour,
          boolean isNightTariff) {
        super();
        this.name = name;
        this.pricePerHour = pricePerHour;
        this.startHour = startHour;
        this.endHour = endHour;
        this.isNightTariff = isNightTariff;
    }

    @Override
    public String toString() {
        return "Tariff{" +
              "name='" + name + '\'' +
              ", pricePerHour=" + pricePerHour +
              ", startHour=" + startHour +
              ", endHour=" + endHour +
              ", isNightTariff=" + isNightTariff +
              '}';
    }
}
