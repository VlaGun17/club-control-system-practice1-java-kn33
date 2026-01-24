package Entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

    @Override
    public String toString() {
        return "Payment{" +
              "sessionId=" + sessionId +
              ", amount=" + amount +
              ", paymentTime=" + paymentTime +
              ", paymentType=" + paymentType +
              '}';
    }
}
