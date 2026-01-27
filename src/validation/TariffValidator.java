package validation;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.entities.Tariff;
import storage.contracts.TariffRepository;

public class TariffValidator implements Validator<Tariff> {

    private static final BigDecimal MIN_PRICE = new BigDecimal("5.00");
    private static final BigDecimal MAX_PRICE = new BigDecimal("500.00");
    private final TariffRepository tariffRepository;

    public TariffValidator(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    @Override
    public ValidationResult validate(Tariff entity) {
        Map<String, List<String>> errors = new HashMap<>();

        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            addError(errors, "name", "Назва тарифу не може бути порожньою.");
        }

        if (entity.getPricePerHour() == null
              || entity.getPricePerHour().compareTo(BigDecimal.ZERO) < 0) {
            addError(errors, "pricePerHour", "Ціна за годину не може бути від'ємною.");
        }

        validateHours(entity, "time", errors);
        validatePricePerHour(entity.getPricePerHour(), "pricePerHour", errors);
        validateNameUniqueness(entity, errors);

        return new ValidationResult(errors);
    }

    private void validateNameUniqueness(Tariff entity, Map<String, List<String>> errors) {
        var existingTariff = tariffRepository.findByName(entity.getName());

        if (!existingTariff.isEmpty()) {
            Tariff existing = existingTariff.get();

            if (!existing.getId().equals(entity.getId())) {
                List<String> nameErrors = errors.getOrDefault("name",
                      new ArrayList<>());
                nameErrors.add("Тариф з ім'ям '" + entity.getName() +
                      "' вже існує");
                errors.put("name", nameErrors);
            }
        }
    }

    private void validatePricePerHour(BigDecimal pricePerHour, String field,
          Map<String, List<String>> errors) {
        if (pricePerHour.compareTo(MIN_PRICE) < 0) {
            addError(errors, field, "Мінімальна ціна: " + MIN_PRICE + " грн/год");
        }
        if (pricePerHour.compareTo(MAX_PRICE) > 0) {
            addError(errors, field, "Максимальна ціна: " + MAX_PRICE + " грн/год");
        }
    }

    private void validateHours(Tariff entity, String field, Map<String, List<String>> errors) {
        LocalTime timeStart = entity.getStartHour();
        LocalTime timeEnd = entity.getEndHour();
        int intHourStart = timeStart.getHour();
        int intHourEnd = timeEnd.getHour();
        if (intHourStart < 0 || intHourEnd > 23) {
            addError(errors, field, "Година повинна бути в діапазоні від 0 до 23.");
        }
        if (!entity.isNightTariff() && !entity.getStartHour().isBefore(entity.getEndHour())) {
            throw new IllegalArgumentException("Час початку повинен бути раніше часу завершення");
        }
    }

    private void addError(Map<String, List<String>> errors, String field, String message) {
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
    }
}
