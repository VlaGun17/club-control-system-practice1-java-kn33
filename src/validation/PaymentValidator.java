package validation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import models.entities.Payment;
import models.enums.PaymentType;
import storage.contracts.PaymentRepository;

public class PaymentValidator implements Validator<Payment> {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000.00");
    PaymentRepository paymentRepository;

    public PaymentValidator(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public ValidationResult validate(Payment entity) {
        Map<String, List<String>> errors = new HashMap<>();

        validateSessionId(entity.getSessionId(), errors);
        validateAmount(entity.getAmount(), errors);
        validatePaymentType(entity.getPaymentType(), errors);
        validatePaymentTime(entity.getPaymentTime(), errors);

        return new ValidationResult(errors);
    }

    private void validateSessionId(UUID sessionId,
          Map<String, List<String>> errors) {
        if (sessionId == null) {
            addError(errors, "sessionId", "ID сесії повинен бути вказаний");
        }
    }

    private void validateAmount(BigDecimal amount,
          Map<String, List<String>> errors) {
        if (amount == null) {
            addError(errors, "amount", "Сума платежу не може бути null");
            return;
        }

        if (amount.compareTo(MIN_AMOUNT) < 0) {
            addError(errors, "amount",
                  "Мінімальна сума платежу: " + MIN_AMOUNT + " грн");
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            addError(errors, "amount",
                  "Максимальна сума платежу: " + MAX_AMOUNT + " грн");
        }
    }

    private void validatePaymentType(PaymentType paymentType,
          Map<String, List<String>> errors) {
        if (paymentType == null) {
            addError(errors, "paymentType", "Тип оплати повинен бути вказаний");
        }
    }

    private void validatePaymentTime(LocalDateTime paymentTime,
          Map<String, List<String>> errors) {
        if (paymentTime == null) {
            addError(errors, "paymentTime", "Час платежу не може бути порожнім");
            return;
        }

        if (paymentTime.isAfter(LocalDateTime.now().plusMinutes(5))) {
            addError(errors, "paymentTime",
                  "Час платежу не може бути в майбутньому");
        }
    }

    private void addError(Map<String, List<String>> errors, String field, String message) {
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
    }
}
