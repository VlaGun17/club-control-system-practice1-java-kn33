package services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import models.entities.Admin;
import storage.contracts.AdminRepository;
import storage.uow.UnitOfWork;
import storage.util.PasswordHasher;
import validation.AdminValidator;
import validation.ValidationResult;

public class AdminService {

    private final AdminRepository adminRepository;
    private final AdminValidator adminValidator;
    private final UnitOfWork<Admin, UUID> uow;
    PasswordHasher passwordHasher;

    public AdminService(AdminRepository adminRepository, PasswordHasher passwordHasher) {
        this.adminRepository = adminRepository;
        this.uow = new UnitOfWork<>(adminRepository, Admin::getId);
        this.adminValidator = new AdminValidator(adminRepository);
        this.passwordHasher = passwordHasher;
    }

    public Admin createAdmin(String login, String password, String email) throws Exception {

        Admin newAdmin = new Admin(login, password, email);

        ValidationResult result = adminValidator.validate(newAdmin);

        String storedHashPass = PasswordHasher.hash(password);
        newAdmin.setPassword(storedHashPass);

        if (result.isValid()) {
            uow.registerNew(newAdmin);
            uow.commit();
            return newAdmin;
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
            return null;
        }
    }

    public void updateAdmin(UUID adminId, String login) {
        Optional<Admin> existingAdmin = adminRepository.findById(adminId);

        if (existingAdmin.isEmpty()) {
            throw new IllegalArgumentException("Адміна з ID " + adminId +
                  " не знайдено");
        }

        Admin admin = existingAdmin.get();
        admin.setLogin(login);

        ValidationResult result = adminValidator.validateForLogin(admin);

        if (result.isValid()) {
            uow.registerDirty(admin);
            uow.commit();
            System.out.println("✓ Адміна успішно змінено");
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public void deleteAdmin(UUID adminId) {
        Optional<Admin> admin = adminRepository.findById(adminId);

        if (admin.isEmpty()) {
            throw new IllegalArgumentException("Адміна з ID " + adminId +
                  " не знайдено");
        }

        uow.registerDeleted(admin.get());
        uow.commit();
    }

    public void changePassword(UUID adminId, String oldPassword, String newPassword)
          throws Exception {
        Optional<Admin> existingAdmin = adminRepository.findById(adminId);

        if (existingAdmin.isEmpty()) {
            throw new IllegalArgumentException("Адміністратора з ID " + adminId +
                  " не знайдено");
        }

        Admin admin = existingAdmin.get();

        if (!PasswordHasher.verify(oldPassword, admin.getPassword())) {
            throw new IllegalArgumentException("Неправильний поточний пароль");
        }

        admin.setPassword(newPassword);

        ValidationResult result = adminValidator.validate(admin);

        if (result.isValid()) {
            String hashedNewPass = PasswordHasher.hash(newPassword);
            admin.setPassword(hashedNewPass);
            uow.registerDirty(admin);
            uow.commit();
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public Optional<Admin> findByLogin(String login) {
        return adminRepository.findByLogin(login);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public boolean existsByLogin(String login) {
        return adminRepository.findByLogin(login).isPresent();
    }
}
