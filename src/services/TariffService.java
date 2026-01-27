package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import models.entities.Tariff;
import storage.contracts.TariffRepository;
import storage.uow.UnitOfWork;
import validation.TariffValidator;
import validation.ValidationResult;

public class TariffService {

    private static final BigDecimal MIN_PRICE = new BigDecimal("5.00");
    private static final BigDecimal MAX_PRICE = new BigDecimal("500.00");
    private final TariffRepository tariffRepository;
    private final TariffValidator tariffValidator;
    private final UnitOfWork<Tariff, UUID> uow;

    public TariffService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
        this.uow = new UnitOfWork<>(tariffRepository, Tariff::getId);
        this.tariffValidator = new TariffValidator(tariffRepository);
    }

    public void createTariff(String name,
          BigDecimal pricePerHour,
          LocalTime startHour,
          LocalTime endHour,
          boolean isNightTariff) {

        Optional<Tariff> existing = tariffRepository.findByName(name);

        Tariff tariff = new Tariff(name, pricePerHour, startHour, endHour, isNightTariff);
        ValidationResult result = tariffValidator.validate(tariff);

        if (result.isValid()) {
            uow.registerNew(tariff);
            uow.commit();
            System.out.println("✓ Тариф успішно збережено.");
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public void deleteTariff(UUID tariffId) {
        Optional<Tariff> tariff = tariffRepository.findById(tariffId);

        if (tariff.isEmpty()) {
            throw new IllegalArgumentException("Комп'ютер з ID " + tariffId +
                  " не знайдено");
        }

        uow.registerDeleted(tariff.get());
        uow.commit();
    }

    public BigDecimal calculateCost(Tariff tariff,
          long minutes,
          BigDecimal discountPercent) {
        BigDecimal hours = BigDecimal.valueOf(minutes)
              .divide(BigDecimal.valueOf(60), 2, RoundingMode.UP);

        BigDecimal baseCost = tariff.getPricePerHour().multiply(hours);

        BigDecimal discount = baseCost
              .multiply(discountPercent)
              .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalCost = baseCost.subtract(discount);

        if (finalCost.compareTo(BigDecimal.ONE) < 0) {
            finalCost = BigDecimal.ONE;
        }

        return finalCost;
    }

    public List<Tariff> getAllTariffs() {
        return tariffRepository.findAll();
    }
}
