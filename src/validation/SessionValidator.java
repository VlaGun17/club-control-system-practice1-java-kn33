package validation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import models.entities.Session;
import models.enums.ComputerStatus;
import storage.contracts.ClientRepository;
import storage.contracts.ComputerRepository;
import storage.contracts.SessionRepository;
import storage.contracts.TariffRepository;

public class SessionValidator implements Validator<Session> {

    private final SessionRepository sessionRepository;
    private final ClientRepository clientRepository;
    private final ComputerRepository computerRepository;
    private final TariffRepository tariffRepository;

    public SessionValidator(SessionRepository sessionRepository,
          ClientRepository clientRepository,
          ComputerRepository computerRepository,
          TariffRepository tariffRepository) {
        this.sessionRepository = sessionRepository;
        this.clientRepository = clientRepository;
        this.computerRepository = computerRepository;
        this.tariffRepository = tariffRepository;
    }

    @Override
    public ValidationResult validate(Session entity) {
        Map<String, List<String>> errors = new HashMap<>();

        validateClientId(entity.getClientId(), errors);
        validateComputerId(entity.getComputerId(), errors);
        validateTariffId(entity.getTariffId(), errors);
        validateStartTime(entity.getStartTime(), errors);
        validateEndTime(entity.getStartTime(), entity.getEndTime(), errors);
        validateTotalCost(entity.getTotalCost(), entity.getEndTime(), errors);

        validateClientHasNoActiveSession(entity.getClientId(), entity.getId(), errors);
        validateComputerIsAvailable(entity.getComputerId(), entity.getId(), errors);

        return new ValidationResult(errors);
    }

    private void validateClientId(UUID clientId,
          Map<String, List<String>> errors) {
        if (clientId == null) {
            addError(errors, "clientId", "Клієнт повинен бути вказаний");
            return;
        }

        if (clientRepository.findById(clientId).isEmpty()) {
            addError(errors, "clientId", "Клієнта з таким ID не знайдено");
        }
    }

    private void validateComputerId(UUID computerId,
          Map<String, List<String>> errors) {
        if (computerId == null) {
            addError(errors, "computerId", "Комп'ютер повинен бути вказаний");
            return;
        }

        if (computerRepository.findById(computerId).isEmpty()) {
            addError(errors, "computerId", "Комп'ютера з таким ID не знайдено");
        }
    }

    private void validateTariffId(UUID tariffId,
          Map<String, List<String>> errors) {
        if (tariffId == null) {
            addError(errors, "tariffId", "Тариф повинен бути обраний");
            return;
        }

        if (tariffRepository.findById(tariffId).isEmpty()) {
            addError(errors, "tariffId", "Тарифу з таким ID не знайдено");
        }
    }

    private void validateStartTime(LocalDateTime startTime,
          Map<String, List<String>> errors) {
        if (startTime == null) {
            addError(errors, "startTime", "Час початку не може бути порожнім");
            return;
        }

        if (startTime.isAfter(LocalDateTime.now().plusMinutes(5))) {
            addError(errors, "startTime",
                  "Час початку не може бути в майбутньому");
        }
    }

    private void validateEndTime(LocalDateTime startTime, LocalDateTime endTime,
          Map<String, List<String>> errors) {
        if (endTime == null) {
            return;
        }

        if (startTime != null && endTime.isBefore(startTime)) {
            addError(errors, "endTime",
                  "Час завершення не може бути раніше часу початку");
        }

        if (endTime.equals(startTime)) {
            addError(errors, "endTime",
                  "Час завершення не може дорівнювати часу початку");
        }

        if (endTime.isAfter(LocalDateTime.now().plusMinutes(5))) {
            addError(errors, "endTime",
                  "Час завершення не може бути в майбутньому");
        }
    }

    private void validateTotalCost(BigDecimal totalCost, LocalDateTime endTime,
          Map<String, List<String>> errors) {
        if (endTime == null && totalCost != null) {
            addError(errors, "totalCost",
                  "Вартість не може бути встановлена для активної сесії");
        }

        if (endTime != null && totalCost == null) {
            addError(errors, "totalCost",
                  "Вартість повинна бути розрахована для завершеної сесії");
        }

        if (totalCost != null && totalCost.compareTo(BigDecimal.ZERO) < 0) {
            addError(errors, "totalCost", "Вартість не може бути від'ємною");
        }
    }

    private void validateClientHasNoActiveSession(UUID clientId, UUID sessionId,
          Map<String, List<String>> errors) {
        if (clientId == null) {
            return;
        }

        sessionRepository.findAll().stream()
              .filter(s -> s.getClientId().equals(clientId))
              .filter(s -> s.getEndTime() == null)
              .filter(s -> !s.getId().equals(sessionId))
              .findFirst()
              .ifPresent(existingSession -> {
                  addError(errors, "clientId",
                        "У клієнта вже є активна сесія на комп'ютері " +
                              existingSession.getComputerId());
              });
    }

    private void validateComputerIsAvailable(UUID computerId, UUID sessionId,
          Map<String, List<String>> errors) {
        if (computerId == null) {
            return;
        }

        computerRepository.findById(computerId).ifPresent(computer -> {
            if (computer.getComputerStatus() == ComputerStatus.OFFLINE) {
                addError(errors, "computerId",
                      "Комп'ютер знаходиться на обслуговуванні");
            }
        });

        sessionRepository.findAll().stream()
              .filter(s -> s.getComputerId().equals(computerId))
              .filter(s -> s.getEndTime() == null)
              .filter(s -> !s.getId().equals(sessionId))
              .findFirst()
              .ifPresent(existingSession -> {
                  addError(errors, "computerId",
                        "На цьому комп'ютері вже є активна сесія клієнта " +
                              existingSession.getClientId());
              });
    }

    private void addError(Map<String, List<String>> errors, String field, String message) {
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
    }
}
