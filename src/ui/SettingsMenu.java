package ui;

import static ui.MainMenu.clearScreen;
import static ui.MainMenu.pause;
import static ui.MainMenu.printHeader;
import static ui.MainMenu.readInt;
import static ui.MainMenu.readLine;

import java.util.Scanner;
import models.entities.Admin;
import services.AdminService;
import services.AuthenticationService;

public class SettingsMenu {

    private final Scanner scanner;
    private final AdminService adminService;
    private final AuthenticationService authService;
    private final Admin currentAdmin;

    public SettingsMenu(Scanner scanner,
          AdminService adminService,
          AuthenticationService authService,
          Admin currentAdmin) {
        this.scanner = scanner;
        this.adminService = adminService;
        this.authService = authService;
        this.currentAdmin = currentAdmin;
    }

    public boolean show() {
        while (true) {
            clearScreen();
            printHeader("НАЛАШТУВАННЯ");
            System.out.println("Поточний адміністратор: " + currentAdmin.getLogin());
            System.out.println();
            System.out.println("1. Змінити пароль");
            System.out.println("2. Оновити дані адміна");
            System.out.println("3. Переглянути всіх адмінів");
            System.out.println("4. Видалити адміна");
            System.out.println("0. Назад");
            System.out.println();

            int choice = readInt("Оберіть пункт: ");

            switch (choice) {
                case 1 -> changePassword();
                case 2 -> updateAdmin();
                case 3 -> viewAllAdmins();
                case 4 -> {
                    if (!deleteAdmin()) {
                        return false;
                    }
                }
                case 0 -> {
                    return true;
                }
                default -> {
                    System.out.println("Невірний вибір!");
                    pause();
                }
            }
        }
    }

    private Admin changePassword() {
        clearScreen();
        printHeader("ЗМІНИТИ ПАРОЛЬ");

        String oldPassword = readLine("Поточний пароль: ");
        String newPassword = readLine("Новий пароль: ");
        String confirmPassword = readLine("Підтвердження нового пароля: ");

        if (!newPassword.equals(confirmPassword)) {
            System.out.println();
            System.out.println("✗ Паролі не співпадають!");
            pause();
            return null;
        }

        if (oldPassword.equals(newPassword)) {
            System.out.println();
            System.out.println("Старий пароль не може співпадати з новим.");
            pause();
            return null;
        }

        try {
            adminService.changePassword(
                  currentAdmin.getId(),
                  oldPassword,
                  newPassword
            );
            System.out.println();
            pause();
            return null;
        } catch (Exception e) {
            System.out.println();
            System.out.println("❌ Помилка: " + e.getMessage());
            pause();
            return null;
        }
    }

    private void updateAdmin() {
        clearScreen();
        printHeader("ОНОВИТИ ДАНІ");

        System.out.println("Поточні дані:");
        System.out.println("Логін: " + currentAdmin.getLogin());
        System.out.println("Email: " + currentAdmin.getEmail());
        System.out.println();

        String newLogin = readLine("Новий login (Enter - залишити без змін): ");

        if (newLogin.isEmpty()) {
            System.out.println("Скасовано.");
            pause();
            return;
        }

        adminService.updateAdmin(
              currentAdmin.getId(),
              newLogin
        );
        pause();
    }

    private void viewAllAdmins() {
        clearScreen();
        printHeader("ВСІ АДМІНІСТРАТОРИ");

        var admins = adminService.getAllAdmins();

        System.out.println("Всього адміністраторів: " + admins.size());
        System.out.println();

        for (Admin admin : admins) {
            System.out.println("─".repeat(50));
            System.out.println("ID: " + admin.getId().toString());
            System.out.println("Логін: " + admin.getLogin());
            System.out.println("Email: " + admin.getEmail());
            if (admin.getId().equals(currentAdmin.getId())) {
                System.out.println("(Це ви)");
            }
            System.out.println("─".repeat(50));
            System.out.println();
        }

        pause();
    }

    private boolean deleteAdmin() {
        clearScreen();
        printHeader("ВИДАЛИТИ АДМІНА");

        var admins = adminService.getAllAdmins();

        if (admins.size() == 1) {
            System.out.println("✗ Не можна видалити останього адміністратора!");
            pause();
            return true;
        }

        System.out.println("Список адміністраторів:");
        for (int i = 0; i < admins.size(); i++) {
            Admin a = admins.get(i);
            String current = a.getId().equals(currentAdmin.getId()) ? " (Ви)" : "";
            System.out.printf("%d. %-20s (%-30s)%s\n",
                  i + 1,
                  a.getLogin(),
                  a.getEmail(),
                  current);
        }

        System.out.println();
        int choice = readInt("Оберіть адміна (0 - скасувати): ");

        if (choice < 1 || choice > admins.size()) {
            System.out.println("Скасовано.");
            pause();
            return true;
        }

        Admin selected = admins.get(choice - 1);

        boolean isSelf = selected.getId().equals(currentAdmin.getId());

        if (isSelf) {
            System.out.println();
            System.out.println("⚠️  УВАГА! Ви видаляєте свій обліковий запис!");
            System.out.println("Після видалення ви будете виведені з системи.");
        }

        System.out.println();
        String confirm = readLine("Видалити адміна '" + selected.getLogin() + "'? (yes/no): ");

        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Скасовано.");
            pause();
            return true;
        }

        adminService.deleteAdmin(selected.getId());

        System.out.println("Адміна " + selected.getLogin() + " видалено.");
        pause();
        return !isSelf;
    }
}
