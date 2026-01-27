package storage.contracts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import models.entities.Payment;
import models.enums.PaymentType;

public interface PaymentRepository extends Repository<Payment> {

    Optional<Payment> findBySessionId(UUID sessionId);

    BigDecimal getTotalRevenue(LocalDateTime date);

    List<Payment> findByType(PaymentType type);
}
