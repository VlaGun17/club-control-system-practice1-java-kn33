package Entities;

import java.math.BigDecimal;
import java.util.Date;

public class Client extends BaseEntity {

    private final String nickname;
    private final String email;
    private final BigDecimal balance;
    private final int visitCount;
    private final BigDecimal discountPercent;
    private final Date registrationDate;

    public Client(String nickname, String email, BigDecimal balance, int visitCount,
          BigDecimal discountPercent, Date registrationDate) {
        super();
        this.nickname = nickname;
        this.email = email;
        this.balance = balance;
        this.visitCount = visitCount;
        this.discountPercent = discountPercent;
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return "Client{" +
              "nickname='" + nickname + '\'' +
              ", email='" + email + '\'' +
              ", balance=" + balance +
              ", visitCount=" + visitCount +
              ", discountPercent=" + discountPercent +
              ", registrationDate=" + registrationDate +
              '}';
    }
}
