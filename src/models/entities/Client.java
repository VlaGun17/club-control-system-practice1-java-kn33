package models.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import models.util.BaseEntity;

public class Client extends BaseEntity {

    private final String nickname;
    private final String email;
    private final LocalDateTime registrationDate;
    private BigDecimal discountPercent;
    private int visitCount;
    private BigDecimal balance;

    public Client(String nickname, String email, BigDecimal balance, int visitCount,
          BigDecimal discountPercent, LocalDateTime registrationDate) {
        super();
        this.nickname = nickname;
        this.email = email;
        this.balance = balance;
        this.visitCount = visitCount;
        this.discountPercent = discountPercent;
        this.registrationDate = registrationDate;
    }

    public Client(UUID id, String nickname, String email,
          BigDecimal balance, int visitCount,
          BigDecimal discountPercent,
          LocalDateTime registrationDate) {
        super(id);
        this.nickname = nickname;
        this.email = email;
        this.balance = balance;
        this.visitCount = visitCount;
        this.discountPercent = discountPercent;
        this.registrationDate = registrationDate;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    @Override
    public String toString() {
        return "Client{" +
              "id=" + getId() +
              ", nickname='" + nickname + '\'' +
              ", email='" + email + '\'' +
              ", balance=" + balance +
              ", visitCount=" + visitCount +
              ", discountPercent=" + discountPercent +
              ", registrationDate=" + registrationDate +
              '}';
    }
}
