package models.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import models.util.BaseEntity;

public class Session extends BaseEntity {

    private final UUID clientId;
    private final UUID computerId;
    private final UUID tariffId;
    private final LocalDateTime startTime;
    private final BigDecimal totalCost;
    private final LocalDateTime endTime;
    private final boolean isActive;

    public Session(UUID clientId, UUID computerId, UUID tariffId) {
        super();
        this.clientId = clientId;
        this.computerId = computerId;
        this.tariffId = tariffId;
        this.startTime = LocalDateTime.now();
        this.totalCost = BigDecimal.ZERO;
        this.endTime = null;
        this.isActive = true;
    }

    public Session(UUID sessionId, UUID clientId, UUID computerId, UUID tariffId,
          LocalDateTime startTime, LocalDateTime endTime, BigDecimal totalCost, boolean isActive) {
        super(sessionId);
        this.clientId = clientId;
        this.computerId = computerId;
        this.tariffId = tariffId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalCost = totalCost;
        this.isActive = isActive;
    }

    public UUID getClientId() {
        return clientId;
    }

    public UUID getComputerId() {
        return computerId;
    }

    public UUID getTariffId() {
        return tariffId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return "Session{" +
              "id=" + getId() +
              ", clientId=" + clientId +
              ", computerId=" + computerId +
              ", tariffId=" + tariffId +
              ", startTime=" + startTime +
              ", endTime=" + endTime +
              ", totalCost=" + totalCost +
              '}';
    }
}
