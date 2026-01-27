package services;

import java.util.Optional;
import models.entities.Admin;
import storage.contracts.AdminRepository;
import storage.util.PasswordHasher;

public class AuthenticationService {

    private final AdminRepository adminRepository;
    private final PasswordHasher passwordHasher;

    private Admin currentAdmin;

    public AuthenticationService(AdminRepository adminRepository,
          PasswordHasher passwordHasher) {
        this.adminRepository = adminRepository;
        this.passwordHasher = passwordHasher;
    }

    public Admin login(String login, String password) throws Exception {
        Optional<Admin> adminOpt = adminRepository.findByLogin(login);
        if (adminOpt.isEmpty()) {
            throw new IllegalArgumentException("Невірний логін або пароль");
        }

        Admin admin = adminOpt.get();

        if (!PasswordHasher.verify(password, admin.getPassword())) {
            throw new IllegalArgumentException("Невірний логін або пароль");
        }

        currentAdmin = admin;
        System.out.println("Вхід виконано. Вітаємо, " + admin.getLogin());
        return currentAdmin;
    }

    public void logout() {
        if (currentAdmin == null) {
            throw new IllegalArgumentException("Немає активної сесії");
        }

        String adminLogin = currentAdmin.getLogin();
        currentAdmin = null;
        System.out.println("Вихід виконано. До побачення, " + adminLogin);
    }
}
