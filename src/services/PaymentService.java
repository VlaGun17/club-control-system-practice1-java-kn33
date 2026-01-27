package services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import models.entities.Payment;
import models.enums.PaymentType;
import storage.contracts.PaymentRepository;
import storage.uow.UnitOfWork;
import validation.PaymentValidator;
import validation.ValidationResult;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;
    private final UnitOfWork<Payment, UUID> uow;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentValidator = new PaymentValidator(paymentRepository);
        this.uow = new UnitOfWork<>(paymentRepository, Payment::getId);
    }

    public void registerPayment(UUID sessionId, BigDecimal amount, PaymentType paymentType) {
        Payment newPayment = new Payment(sessionId, amount, paymentType);

        ValidationResult result = paymentValidator.validate(newPayment);

        if (result.isValid()) {
            uow.registerNew(newPayment);
            uow.commit();
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public List<Payment> findPaymentsBetween(LocalDateTime start,
          LocalDateTime end) {
        return paymentRepository.findAll().stream()
              .filter(p -> !p.getPaymentTime().isBefore(start) &&
                    !p.getPaymentTime().isAfter(end))
              .toList();
    }

    public PaymentStatistics getStatistics(LocalDateTime start, LocalDateTime end) {
        List<Payment> payments = findPaymentsBetween(start, end);

        int totalPayments = payments.size();

        BigDecimal totalAmount = payments.stream()
              .map(Payment::getAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cashAmount = payments.stream()
              .filter(p -> p.getPaymentType() == PaymentType.CASH)
              .map(Payment::getAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cardAmount = payments.stream()
              .filter(p -> p.getPaymentType() == PaymentType.BY_CARD)
              .map(Payment::getAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PaymentStatistics(
              totalPayments,
              totalAmount,
              cashAmount,
              cardAmount
        );
    }
}
