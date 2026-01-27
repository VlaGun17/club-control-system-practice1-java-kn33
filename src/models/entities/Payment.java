package models.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import models.enums.PaymentType;
import models.util.BaseEntity;

public class Payment extends BaseEntity {

    private final UUID sessionId;
    private final BigDecimal amount;
    private final LocalDateTime paymentTime;
    private final PaymentType paymentType;

    public Payment(UUID sessionId, BigDecimal amount, PaymentType paymentType) {
        super();
        this.sessionId = sessionId;
        this.amount = amount;
        this.paymentTime = LocalDateTime.now();
        this.paymentType = paymentType;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    @Override
    public String toString() {
        return "Payment{" +
              "id=" + getId() +
              ", sessionId=" + sessionId +
              ", amount=" + amount +
              ", paymentTime=" + paymentTime +
              ", paymentType=" + paymentType +
              '}';
    }
}
