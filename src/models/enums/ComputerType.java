package models.enums;

public enum ComputerType {
    STANDART("Стандарт"),
    VIP("VIP");

    private final Object type;

    ComputerType(String type) {
        this.type = type;
    }
}
