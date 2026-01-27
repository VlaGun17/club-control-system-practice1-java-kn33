package services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import models.entities.Computer;
import models.enums.ComputerStatus;
import models.enums.ComputerType;
import storage.contracts.ComputerRepository;
import storage.uow.UnitOfWork;
import validation.ComputerValidator;
import validation.ValidationResult;

public class ComputerService {

    private final ComputerRepository computerRepository;
    private final ComputerValidator computerValidator;
    private final UnitOfWork<Computer, UUID> uow;

    public ComputerService(ComputerRepository computerRepository) {
        this.computerRepository = computerRepository;
        this.computerValidator = new ComputerValidator(computerRepository);
        this.uow = new UnitOfWork<>(computerRepository, Computer::getId);
    }

    public void createComputer(int number, ComputerType computerType) {
        Computer newComputer = new Computer(number, computerType, ComputerStatus.FREE);

        ValidationResult result = computerValidator.validate(newComputer);

        if (result.isValid()) {
            uow.registerNew(newComputer);
            uow.commit();
            System.out.println("✓ Комп'ютер успішно збережено");
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public void updateComputerStatus(UUID computerId, ComputerStatus status) {
        Optional<Computer> existingComputer = computerRepository.findById(computerId);

        if (existingComputer.isEmpty()) {
            throw new IllegalArgumentException("Комп'ютер з ID " + computerId +
                  " не знайдено");
        }

        Computer computer = existingComputer.get();
        computer.setComputerStatus(status);

        uow.registerDirty(computer);
        uow.commit();
    }

    public void deleteComputer(UUID computerId) {
        Optional<Computer> computer = computerRepository.findById(computerId);

        if (computer.isEmpty()) {
            throw new IllegalArgumentException("Комп'ютер з ID " + computerId +
                  " не знайдено");
        }

        if (computer.get().getComputerStatus() == ComputerStatus.BUSY) {
            throw new IllegalArgumentException(
                  "Неможливо видалити комп'ютер, який зараз використовується");
        }

        uow.registerDeleted(computer.get());
        uow.commit();
    }

    public void occupyComputer(UUID computerId) {
        Optional<Computer> computerOpt = computerRepository.findById(computerId);
        if (computerOpt.isEmpty()) {
            throw new IllegalArgumentException("Комп'ютер не знайдено");
        }

        Computer computer = computerOpt.get();
        if (computer.getComputerStatus() != ComputerStatus.FREE) {
            throw new IllegalArgumentException("\"Комп'ютер недоступний. Поточний статус: \" +\n"
                  + "                        computer.getComputerStatus()");
        }

        computer.setComputerStatus(ComputerStatus.BUSY);
        uow.registerDirty(computer);
        uow.commit();
    }

    public void freeComputer(UUID computerId) {
        Optional<Computer> computerOpt = computerRepository.findById(computerId);
        if (computerOpt.isEmpty()) {
            throw new IllegalArgumentException("Комп'ютер не знайдено");
        }

        Computer computer = computerOpt.get();

        if (computer.getComputerStatus() != ComputerStatus.BUSY) {
            throw new IllegalArgumentException("Комп'ютер не зайнятий. Поточний статус: " +
                  computer.getComputerStatus());
        }

        computer.setComputerStatus(ComputerStatus.FREE);
        uow.registerDirty(computer);
        uow.commit();
    }

    public List<Computer> getAllComputers() {
        return computerRepository.findAll();
    }

    public boolean isAvailable(UUID computerId) {
        Optional<Computer> computerOpt = computerRepository.findById(computerId);
        if (computerOpt.isEmpty()) {
            return false;
        }
        return computerOpt.get().getComputerStatus() == ComputerStatus.FREE;
    }
}
