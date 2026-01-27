package storage.contracts;

import java.util.List;
import java.util.Optional;
import models.entities.Computer;
import models.enums.ComputerStatus;
import models.enums.ComputerType;

public interface ComputerRepository extends Repository<Computer> {

    List<Computer> findByComputerType(ComputerType computerType);

    List<Computer> findByComputerStatus(ComputerStatus computerStatus);

    Optional<Computer> findByNumber(int number);
}
