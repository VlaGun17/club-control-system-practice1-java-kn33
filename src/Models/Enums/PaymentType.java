package Entities;

public enum PaymentType {
    BY_CARD("Карткою"),
    CASH("Готівка");

    private final Object type;

    PaymentType(String type) {
        this.type = type;
    }
}
