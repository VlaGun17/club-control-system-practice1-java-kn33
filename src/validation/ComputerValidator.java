package validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.entities.Computer;
import models.enums.ComputerStatus;
import models.enums.ComputerType;
import storage.contracts.ComputerRepository;

public class ComputerValidator implements Validator<Computer> {

    private static final int MIN_COMPUTER_NUMBER = 1;
    private static final int MAX_COMPUTER_NUMBER = 9999;
    private final ComputerRepository computerRepository;

    public ComputerValidator(ComputerRepository computerRepository) {
        this.computerRepository = computerRepository;
    }

    public ValidationResult validate(Computer entity) {
        Map<String, List<String>> errors = new HashMap<>();

        validateNumber(entity.getNumber(), errors);
        validateComputerType(entity.getComputerType(), errors);
        validateComputerStatus(entity.getComputerStatus(), errors);

        validateNumberUniqueness(entity, errors);

        return new ValidationResult(errors);
    }

    private void validateNumberUniqueness(Computer entity, Map<String, List<String>> errors) {
        var existingComputer = computerRepository.findByNumber(entity.getNumber());

        if (existingComputer.isPresent()) {
            Computer existing = existingComputer.get();

            if (!existing.getId().equals(entity.getId())) {
                List<String> numberErrors = errors.getOrDefault("number",
                      new ArrayList<>());
                numberErrors.add("Комп'ютер з номером '" + entity.getNumber() +
                      "' вже існує");
                errors.put("number", numberErrors);
            }
        }
    }

    private void validateNumber(int number, Map<String, List<String>> errors) {
        List<String> numberErrors = new ArrayList<>();

        if (number < MIN_COMPUTER_NUMBER) {
            numberErrors.add("Номер комп'ютера повинен бути не менше " + MIN_COMPUTER_NUMBER);
        }

        if (number > MAX_COMPUTER_NUMBER) {
            numberErrors.add("Номер комп'ютера повинен бути не більше " + MAX_COMPUTER_NUMBER);
        }

        if (!numberErrors.isEmpty()) {
            errors.put("number", numberErrors);
        }
    }

    private void validateComputerType(ComputerType computerType, Map<String, List<String>> errors) {
        List<String> typeErrors = new ArrayList<>();

        if (computerType == null) {
            typeErrors.add("Тип комп'ютера не може бути порожнім");
        }

        if (!typeErrors.isEmpty()) {
            errors.put("computerType", typeErrors);
        }
    }

    private void validateComputerStatus(ComputerStatus computerStatus,
          Map<String, List<String>> errors) {
        List<String> statusErrors = new ArrayList<>();

        if (computerStatus == null) {
            statusErrors.add("Статус комп'ютера не може бути порожнім");
        }

        if (!statusErrors.isEmpty()) {
            errors.put("computerStatus", statusErrors);
        }
    }
}

