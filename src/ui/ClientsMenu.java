package ui;

import static ui.MainMenu.clearScreen;
import static ui.MainMenu.pause;
import static ui.MainMenu.printHeader;
import static ui.MainMenu.readInt;
import static ui.MainMenu.readLine;

import java.math.BigDecimal;
import java.util.Scanner;
import models.entities.Client;
import services.ClientService;
import services.EmailService;

public class ClientsMenu {

    private final Scanner scanner;
    private final ClientService clientService;

    public ClientsMenu(Scanner scanner, ClientService clientService) {
        this.scanner = scanner;
        this.clientService = clientService;
    }

    public void show() {
        while (true) {
            clearScreen();
            printHeader("КЛІЄНТИ");
            System.out.println("1. Створити нового клієнта");
            System.out.println("2. Поповнити баланс");
            System.out.println("3. Зняти суму");
            System.out.println("4. Встановити знижку");
            System.out.println("5. Переглянути клієнтів");
            System.out.println("0. Назад");
            System.out.println();

            int choice = readInt("Оберіть пункт: ");

            switch (choice) {
                case 1 -> createClient();
                case 2 -> addBalance();
                case 3 -> deductBalance();
                case 4 -> setDiscount();
                case 5 -> viewClientsMenu();
                case 0 -> {
                    return;
                }
                default -> {
                    System.out.println("Невірний вибір!");
                    pause();
                }
            }
        }
    }

    private void createClient() {
        EmailService emailService = new EmailService();
        clearScreen();
        printHeader("СТВОРИТИ КЛІЄНТА");

        System.out.println("Вимоги до нікнейму:");
        System.out.println("  • 3-50 символів");
        System.out.println("  • Тільки латинські літери, цифри, підкреслення");
        System.out.println();

        String nickname = readLine("Нікнейм: ");
        String email = readLine("Email: ");

        if (clientService.createClient(nickname, email) != null) {
            emailService.sendWelcomeEmailToClient(email, nickname);
            System.out.println();
            pause();
        }
    }

    private void addBalance() {
        Client client = selectClient("ПОПОВНИТИ БАЛАНС");
        if (client == null) {
            return;
        }

        System.out.println();
        System.out.println("Поточний баланс: " + client.getBalance() + " грн");
        System.out.println();

        String amountStr = readLine("Сума поповнення (грн): ");
        BigDecimal amount;

        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            System.out.println("✗ Некоректна сума!");
            pause();
            return;
        }

        clientService.addBalance(client.getId(), amount);
        System.out.println();
        pause();
    }

    private void deductBalance() {
        Client client = selectClient("ЗНЯТИ СУМУ");
        if (client == null) {
            return;
        }

        System.out.println();
        System.out.println("Поточний баланс: " + client.getBalance() + " грн");
        System.out.println();

        String amountStr = readLine("Сума для зняття (грн): ");
        BigDecimal amount;

        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            System.out.println("✗ Некоректна сума!");
            pause();
            return;
        }

        clientService.deductBalance(client.getId(), amount);
        System.out.println();
        pause();
    }

    private void setDiscount() {
        Client client = selectClient("ВСТАНОВИТИ ЗНИЖКУ");
        if (client == null) {
            return;
        }

        System.out.println();
        System.out.println("Поточна знижка: " + client.getDiscountPercent() + "%");
        System.out.println();

        String discountStr = readLine("Нова знижка (%): ");
        BigDecimal discount = null;

        try {
            discount = new BigDecimal(discountStr);
        } catch (NumberFormatException e) {
            System.out.println("✗ Некоректне значення!");
            pause();
        }

        clientService.setCustomDiscount(
              client.getId(), discount);
        System.out.println();
        pause();
    }

    private void viewClientsMenu() {
        while (true) {
            clearScreen();
            printHeader("ПЕРЕГЛЯНУТИ КЛІЄНТІВ");
            System.out.println("1. VIP клієнти");
            System.out.println("2. Нові користувачі за N днів");
            System.out.println("3. Всі клієнти");
            System.out.println("0. Назад");
            System.out.println();

            int choice = readInt("Оберіть пункт: ");

            switch (choice) {
                case 1 -> viewVipClients();
                case 2 -> viewNewClients();
                case 3 -> viewAllClients();
                case 0 -> {
                    return;
                }
                default -> {
                    System.out.println("Невірний вибір!");
                    pause();
                }
            }
        }
    }

    private void viewVipClients() {
        clearScreen();
        printHeader("VIP КЛІЄНТИ");

        var clients = clientService.findVipClients();

        if (clients.isEmpty()) {
            System.out.println("VIP клієнтів немає.");
        } else {
            System.out.println("Знайдено VIP клієнтів: " + clients.size());
            System.out.println();

            for (Client client : clients) {
                printClient(client);
                System.out.println();
            }
        }

        pause();
    }

    private void viewNewClients() {
        clearScreen();
        printHeader("НОВІ КОРИСТУВАЧІ");

        int days = readInt("За скільки днів показати: ");

        var clients = clientService.findNewClients(days);

        System.out.println();
        if (clients.isEmpty()) {
            System.out.println("Нових клієнтів за " + days + " днів немає.");
        } else {
            System.out.println("Знайдено нових клієнтів: " + clients.size());
            System.out.println();

            for (Client client : clients) {
                printClient(client);
                System.out.println();
            }
        }

        pause();
    }

    private void viewAllClients() {
        clearScreen();
        printHeader("ВСІ КЛІЄНТИ");

        var clients = clientService.getAllClients();

        if (clients.isEmpty()) {
            System.out.println("Клієнтів немає.");
        } else {
            System.out.println("Всього клієнтів: " + clients.size());
            System.out.println();

            for (Client client : clients) {
                printClient(client);
                System.out.println();
            }
        }

        pause();
    }

    private Client selectClient(String title) {
        clearScreen();
        printHeader(title);

        var clients = clientService.getAllClients();

        if (clients.isEmpty()) {
            System.out.println("Клієнтів немає.");
            pause();
            return null;
        }

        System.out.println("Список клієнтів:");
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            System.out.printf("%d. %-20s (%-30s) Баланс: %8s грн\n",
                  i + 1,
                  c.getNickname(),
                  c.getEmail(),
                  c.getBalance());
        }

        System.out.println();
        int choice = readInt("Оберіть клієнта (0 - скасувати): ");

        if (choice < 1 || choice > clients.size()) {
            System.out.println("Скасовано.");
            pause();
            return null;
        }

        return clients.get(choice - 1);
    }

    private void printClient(Client client) {
        System.out.println("─".repeat(50));
        System.out.println("ID: " + client.getId().toString());
        System.out.println("Нікнейм: " + client.getNickname());
        System.out.println("Email: " + client.getEmail());
        System.out.println("Баланс: " + client.getBalance() + " грн");
        System.out.println("Відвідувань: " + client.getVisitCount());
        System.out.println("Знижка: " + client.getDiscountPercent() + "%");
        System.out.println("Реєстрація: " +
              client.getRegistrationDate().toLocalDate());
        System.out.println("─".repeat(50));
    }
}
