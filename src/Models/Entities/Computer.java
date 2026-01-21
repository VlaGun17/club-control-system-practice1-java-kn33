package Entities;

public class Computer extends BaseEntity {

    private final int number;
    private final ComputerType computerType;
    private final ComputerStatus computerStatus;

    public Computer(int number, ComputerType computerType, ComputerStatus computerStatus) {
        super();
        this.number = number;
        this.computerType = computerType;
        this.computerStatus = computerStatus;
    }

    @Override
    public String toString() {
        return "Computer{" +
              "number=" + number +
              ", computerType=" + computerType +
              ", computerStatus=" + computerStatus +
              '}';
    }
}
