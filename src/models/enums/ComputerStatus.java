package models.enums;

public enum ComputerStatus {
    FREE("Вільний"),
    BUSY("Зайнятий"),
    OFFLINE("Вимкнений");

    private final Object status;

    ComputerStatus(String status) {
        this.status = status;
    }
}
