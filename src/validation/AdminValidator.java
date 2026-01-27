package validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import models.entities.Admin;
import storage.contracts.AdminRepository;

public class AdminValidator implements Validator<Admin> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
          "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASS_PATTERN = Pattern.compile(
          "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private static final int MIN_LOGIN_LENGTH = 3;
    private static final int MAX_LOGIN_LENGTH = 50;
    private final AdminRepository adminRepository;

    public AdminValidator(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public ValidationResult validateForLogin(Admin entity) {
        Map<String, List<String>> errors = new HashMap<>();

        validateLogin(entity.getLogin(), errors);
        validateLoginUniqueness(entity, errors);

        return new ValidationResult(errors);
    }

    public ValidationResult validate(Admin entity) {
        Map<String, List<String>> errors = new HashMap<>();

        validateLogin(entity.getLogin(), errors);
        validatePassword(entity.getPassword(), errors);
        validateEmail(entity.getEmail(), errors);
        validateLoginUniqueness(entity, errors);

        return new ValidationResult(errors);
    }

    public void validateLoginUniqueness(Admin entity, Map<String, List<String>> errors) {
        var existingAdmin = adminRepository.findByLogin(entity.getLogin());

        if (existingAdmin.isPresent()) {
            Admin existing = existingAdmin.get();

            if (!existing.getId().equals(entity.getId())) {
                List<String> loginErrors = errors.getOrDefault("login",
                      new ArrayList<>());
                loginErrors.add("Адміністратор з login '" + entity.getLogin() +
                      "' вже існує");
                errors.put("login", loginErrors);
            }
        }
    }

    public void validateLogin(String login, Map<String, List<String>> errors) {
        List<String> loginErrors = new ArrayList<>();

        if (login == null || login.trim().isEmpty()) {
            loginErrors.add("Логін не може бути порожнім.");
        } else {
            if (login.length() < MIN_LOGIN_LENGTH) {
                loginErrors.add(
                      "Логін повинен мати мінімум " + MIN_LOGIN_LENGTH + " символів.");
            }
            if (login.length() > MAX_LOGIN_LENGTH) {
                loginErrors.add(
                      "Логін повинен мати максимум " + MAX_LOGIN_LENGTH + " символів.");
            }
            if (!login.matches("^[a-zA-Z0-9_]{3,50}$")) {
                loginErrors.add(
                      "Логін повинен мати тільки літери, цифри, нижнє підкреслення.");
            }
        }

        if (!loginErrors.isEmpty()) {
            errors.put("login", loginErrors);
        }
    }

    private void validatePassword(String password, Map<String, List<String>> errors) {
        List<String> passwordErrors = new ArrayList<>();

        if (password == null || password.trim().isEmpty()) {
            passwordErrors.add("Пароль не може бути порожнім.");
        } else if (!PASS_PATTERN.matcher(password).matches()) {
            passwordErrors.add("Пароль повинен містити від 8 до 20 символів, \n" +
                  "як мінімум одну цифру, одну букву та спеціальний символ");
        }

        if (!passwordErrors.isEmpty()) {
            errors.put("password", passwordErrors);
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
}
