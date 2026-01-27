package services;

import java.math.BigDecimal;

public record PaymentStatistics(
      long totalPayments,
      BigDecimal totalAmount,
      BigDecimal cashAmount,
      BigDecimal cardAmount
) {

}
