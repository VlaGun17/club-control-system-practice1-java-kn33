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
import services.EmailService;

public class AuthMenu {

    private final Scanner scanner;
    private final AuthenticationService authService;
    private final AdminService adminService;

    public AuthMenu(Scanner scanner,
          AuthenticationService authService,
          AdminService adminService) {
        this.scanner = scanner;
        this.authService = authService;
        this.adminService = adminService;
    }

    public Admin show() throws Exception {
        while (true) {
            clearScreen();
            printHeader("АВТОРИЗАЦІЯ");
            System.out.println("1. Вхід");
            System.out.println("2. Реєстрація");
            System.out.println("0. Вихід");
            System.out.println();

            int choice = readInt("Оберіть пункт: ");

            switch (choice) {
                case 1 -> {
                    Admin admin = handleLogin();
                    if (admin != null) {
                        return admin;
                    }
                }
                case 2 -> {
                    Admin admin = handleRegistration();
                    if (admin != null) {
                        return admin;
                    }
                }
                case 0 -> {
                    return null;
                }
                default -> {
                    System.out.println("Невірний вибір!");
                    pause();
                }
            }
        }
    }

    private Admin handleLogin() {
        clearScreen();
        printHeader("ВХІД В СИСТЕМУ");

        String login = readLine("Логін: ");
        String password = readLine("Пароль: ");
        try {
            Admin admin = authService.login(login, password);
            System.out.println();
            System.out.println("✓ Успішна авторизація");
            pause();
            return admin;
        } catch (Exception e) {
            System.out.println();
            System.out.println("❌ Помилка: " + e.getMessage());
            pause();
            return null;
        }
    }

    private Admin handleRegistration() {
        EmailService emailService = new EmailService();
        clearScreen();
        printHeader("РЕЄСТРАЦІЯ АДМІНІСТРАТОРА");

        System.out.println("Вимоги до логіну:");
        System.out.println("  • 3-50 символів");
        System.out.println("  • Тільки латинські літери, цифри, підкреслення");
        System.out.println();

        String login = readLine("Логін: ");

        System.out.println();
        System.out.println("Вимоги до пароля:");
        System.out.println("  • Мінімум 8 символів");
        System.out.println("  • Рекомендується: великі та малі літери, цифри, спецсимволи");
        System.out.println();

        String password = readLine("Пароль: ");
        String passwordConfirm = readLine("Підтвердження пароля: ");

        if (!password.equals(passwordConfirm)) {
            System.out.println();
            System.out.println("✗ Паролі не співпадають!");
            pause();
            return null;
        }

        String email = readLine("Email: ");

        try {
            if (adminService.createAdmin(login, password, email) != null) {
                emailService.sendWelcomeEmailToAdmin(email, login);
                System.out.println();
                System.out.println("✓ Успішна реєстрація");
                System.out.println();
                System.out.println("Тепер увійдіть з новими даними.");
                pause();

            }
        } catch (Exception e) {
            System.out.println();
            System.out.println("❌ Помилка: " + e.getMessage());
            pause();
        }
        return null;
    }
}
