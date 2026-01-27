package models.entities;

import models.enums.ComputerStatus;
import models.enums.ComputerType;
import models.util.BaseEntity;

public class Computer extends BaseEntity {

    private final int number;
    private final ComputerType computerType;
    private ComputerStatus computerStatus;

    public Computer(int number, ComputerType computerType, ComputerStatus computerStatus) {
        super();
        this.number = number;
        this.computerType = computerType;
        this.computerStatus = computerStatus;
    }

    public int getNumber() {
        return number;
    }

    public ComputerType getComputerType() {
        return computerType;
    }

    public ComputerStatus getComputerStatus() {
        return computerStatus;
    }

    public void setComputerStatus(ComputerStatus status) {
        this.computerStatus = status;
    }

    @Override
    public String toString() {
        return "Computer{" +
              "id=" + getId() +
              ", number=" + number +
              ", computerType=" + computerType +
              ", computerStatus=" + computerStatus +
              '}';
    }
}
