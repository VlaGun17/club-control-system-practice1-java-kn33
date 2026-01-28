package validation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import models.entities.Client;
import storage.contracts.ClientRepository;

public class ClientValidator implements Validator<Client> {

    private static final Pattern EMAIL_PATTERN =
          Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_NICKNAME_LENGTH = 3;
    private static final int MAX_NICKNAME_LENGTH = 50;
    private static final BigDecimal MIN_BALANCE = BigDecimal.ZERO;
    private static final BigDecimal MAX_DISCOUNT = new BigDecimal("100");
    private static final BigDecimal MIN_DISCOUNT = BigDecimal.ZERO;
    private static final BigDecimal MIN_DEPOSIT = new BigDecimal("10.00");
    private static final BigDecimal MAX_DEPOSIT = new BigDecimal("10000.00");

    private final ClientRepository repository;

    public ClientValidator(ClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public ValidationResult validate(Client entity) {
        Map<String, List<String>> errors = new HashMap<>();

        validateNickname(entity.getNickname(), errors);
        validateEmail(entity.getEmail(), errors);
        validateBalance(entity.getBalance(), errors);
        validateVisitCount(entity.getVisitCount(), errors);
        validateDiscountPercent(entity.getDiscountPercent(), errors);
        validateRegistrationDate(entity.getRegistrationDate(), errors);

        validateEmailUniqueness(entity, errors);
        validateNicknameUniqueness(entity, errors);

        return new ValidationResult(errors);
    }

    public ValidationResult validateDeduct(Client client, BigDecimal amount) {
        Map<String, List<String>> errors = new HashMap<>();

        validateDeductBalance(client, amount, errors);

        return new ValidationResult(errors);
    }

    public ValidationResult validateAddBalance(Client client, BigDecimal amount) {
        Map<String, List<String>> errors = new HashMap<>();

        validateAddBalance(client, amount, errors);

        return new ValidationResult(errors);
    }

    public ValidationResult validateRegisterVisit(Client client) {
        Map<String, List<String>> errors = new HashMap<>();

        validateVisitCount(client.getVisitCount(), errors);
        validateDiscountPercent(client.getDiscountPercent(), errors);

        return new ValidationResult(errors);
    }

    private void validateEmailUniqueness(Client entity,
          Map<String, List<String>> errors) {
        var existingClient = repository.findByEmail(entity.getEmail());

        if (existingClient.isPresent()) {
            Client existing = existingClient.get();

            if (!existing.getId().equals(entity.getId())) {
                List<String> emailErrors = errors.getOrDefault("email",
                      new ArrayList<>());
                emailErrors.add("Клієнт з email '" + entity.getEmail() +
                      "' вже існує");
                errors.put("email", emailErrors);
            }
        }
    }

    private void validateNicknameUniqueness(Client entity, Map<String, List<String>> errors) {
        var existingClient = repository.findByNameContaining(entity.getNickname());
        List<Client> clients = repository.findAll();

        if (!existingClient.isEmpty()) {
            Client existing = existingClient.getFirst();

            for (Client client : clients) {
                if (existing.getNickname().equals(client.getNickname())) {
                    List<String> nicknameErrors = errors.getOrDefault("nickname",
                          new ArrayList<>());
                    nicknameErrors.add("Клієнт з nickname '" + entity.getNickname() +
                          "' вже існує");
                    errors.put("nickname", nicknameErrors);
                    break;
                }
            }
        }
    }

    private void validateNickname(String nickname, Map<String, List<String>> errors) {
        List<String> nicknameErrors = new ArrayList<>();

        if (nickname == null || nickname.trim().isEmpty()) {
            nicknameErrors.add("Нікнейм не може бути порожнім.");
        } else {
            if (nickname.length() < MIN_NICKNAME_LENGTH) {
                nicknameErrors.add(
                      "Нікнейм повинен мати мінімум " + MIN_NICKNAME_LENGTH + " символів.");
            }
            if (nickname.length() > MAX_NICKNAME_LENGTH) {
                nicknameErrors.add(
                      "Нікнейм повинен мати максимум " + MAX_NICKNAME_LENGTH + " символів.");
            }
            if (!nickname.matches("^[a-zA-Z0-9_]{3,50}$")) {
                nicknameErrors.add(
                      "Нікнейм повинен мати тільки літери, цифри, нижнє підкреслення.");
            }
        }

        if (!nicknameErrors.isEmpty()) {
            errors.put("nickname", nicknameErrors);
        }
    }

    private void validateEmail(String email, Map<String, List<String>> errors) {
        List<String> emailErrors = new ArrayList<>();

        if (email == null || email.trim().isEmpty()) {
            emailErrors.add("Email не може бути порожнім.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailErrors.add("Некоректний формат email.");
        }

        if (!emailErrors.isEmpty()) {
            errors.put("email", emailErrors);
        }
    }

    private void validateBalance(BigDecimal balance, Map<String, List<String>> errors) {
        List<String> balanceErrors = new ArrayList<>();

        if (balance == null) {
            balanceErrors.add("Баланс не може бути нульовим");
        } else if (balance.compareTo(MIN_BALANCE) < 0) {
            balanceErrors.add("Баланс не може бути мінусовим");
        }

        if (!balanceErrors.isEmpty()) {
            errors.put("balance", balanceErrors);
        }
    }

    private void validateDeductBalance(Client client, BigDecimal amount,
          Map<String, List<String>> errors) {
        List<String> balanceErrors = new ArrayList<>();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            balanceErrors.add("Сума списання повинна бути позитивною");
        }

        if (client.getBalance().compareTo(amount) < 0) {
            balanceErrors.add(
                  "Недостатньо коштів. Баланс: " + client.getBalance() +
                        " грн, потрібно: " + amount + " грн");
        }

        if (!balanceErrors.isEmpty()) {
            errors.put("balance", balanceErrors);
        }
    }

    private void validateAddBalance(Client client, BigDecimal amount,
          Map<String, List<String>> errors) {
        List<String> balanceErrors = new ArrayList<>();

        if (amount.compareTo(MIN_DEPOSIT) < 0) {
            balanceErrors.add(
                  "Мінімальна сума поповнення: " + MIN_DEPOSIT + " грн");
        }
        if (amount.compareTo(MAX_DEPOSIT) > 0) {
            balanceErrors.add(
                  "Максимальна сума поповнення: " + MAX_DEPOSIT + " грн");
        }

        if (!balanceErrors.isEmpty()) {
            errors.put("balance", balanceErrors);
        }
    }

    private void validateVisitCount(int visitCount, Map<String, List<String>> errors) {
        List<String> visitCountErrors = new ArrayList<>();

        if (visitCount < 0) {
            visitCountErrors.add("Кількість відвідувань не може бути мінусова");
        }

        if (!visitCountErrors.isEmpty()) {
            errors.put("visitCount", visitCountErrors);
        }
    }

    private void validateDiscountPercent(BigDecimal discountPercent,
          Map<String, List<String>> errors) {
        List<String> discountErrors = new ArrayList<>();

        if (discountPercent == null) {
            discountErrors.add("Знижка не може бути порожня.");
        } else {
            if (discountPercent.compareTo(MIN_DISCOUNT) < 0) {
                discountErrors.add("Знижка не може бути мінусова");
            }
            if (discountPercent.compareTo(MAX_DISCOUNT) > 0) {
                discountErrors.add("Знижка не може перевищувати 100%");
            }
        }

        if (!discountErrors.isEmpty()) {
            errors.put("discountPercent", discountErrors);
        }
    }

    private void validateRegistrationDate(LocalDateTime registrationDate,
          Map<String, List<String>> errors) {
        List<String> dateErrors = new ArrayList<>();

        if (registrationDate == null) {
            dateErrors.add("Дата регістрація не може бути порожнім");
        } else if (registrationDate.isAfter(LocalDateTime.now())) {
            dateErrors.add("Дата регістрації не може бути в майбутньому.");
        }

        if (!dateErrors.isEmpty()) {
            errors.put("registrationDate", dateErrors);
        }
    }
}
