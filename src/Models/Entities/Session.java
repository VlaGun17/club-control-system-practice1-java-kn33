package Entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Session extends BaseEntity {

    private final UUID clientId;
    private final UUID computerId;
    private final UUID tariffId;
    private final LocalDateTime startTime;
    private final BigDecimal totalCost;
    private LocalDateTime endTime;

    public Session(UUID clientId, UUID computerId, UUID tariffId) {
        super();
        this.clientId = clientId;
        this.computerId = computerId;
        this.tariffId = tariffId;
        this.startTime = LocalDateTime.now();
        this.totalCost = BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "Session{" +
              "clientId=" + clientId +
              ", computerId=" + computerId +
              ", tariffId=" + tariffId +
              ", startTime=" + startTime +
              ", endTime=" + endTime +
              ", totalCost=" + totalCost +
              '}';
    }
}
