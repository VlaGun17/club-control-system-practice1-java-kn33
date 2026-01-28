package ui;

import static ui.MainMenu.clearScreen;
import static ui.MainMenu.pause;
import static ui.MainMenu.printHeader;
import static ui.MainMenu.readInt;
import static ui.MainMenu.readLine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import models.entities.Client;
import models.entities.Computer;
import models.entities.Session;
import models.entities.Tariff;
import models.enums.ComputerStatus;
import models.enums.ComputerType;
import services.ClientService;
import services.ComputerService;
import services.PaymentService;
import services.SessionService;
import services.TariffService;

public class SystemMenu {

    private final Scanner scanner;
    private final ComputerService computerService;
    private final SessionService sessionService;
    private final PaymentService paymentService;
    private final ClientService clientService;
    private final TariffService tariffService;

    public SystemMenu(Scanner scanner,
          ComputerService computerService,
          SessionService sessionService,
          PaymentService paymentService,
          ClientService clientService,
          TariffService tariffService) {
        this.scanner = scanner;
        this.computerService = computerService;
        this.sessionService = sessionService;
        this.paymentService = paymentService;
        this.clientService = clientService;
        this.tariffService = tariffService;
    }

    public void show() {
        while (true) {
            clearScreen();
            printHeader("СИСТЕМА");
            System.out.println("1. Переглянути зал");
            System.out.println("2. Додати комп'ютер");
            System.out.println("3. Видалити комп'ютер");
            System.out.println("4. Подивитись активні сесії");
            System.out.println("5. Подивитись збережені сесії за період");
            System.out.println("6. Примусове завершення сесії");
            System.out.println("7. Розпочати сесію.");
            System.out.println("8. Подивитись статистику за період");
            System.out.println("0. Назад");
            System.out.println();

            int choice = readInt("Оберіть пункт: ");

            switch (choice) {
                case 1 -> viewComputerHall();
                case 2 -> addComputer();
                case 3 -> deleteComputer();
                case 4 -> viewActiveSessions();
                case 5 -> viewSessionsByPeriod();
                case 6 -> forceEndSession();
                case 7 -> startSession();
                case 8 -> viewStatistics();
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

    private void startSession() {
        clearScreen();
        printHeader("РОЗПОЧАТИ СЕСІЮ");

        var computers = computerService.getAllComputers();
        var activeSessions = sessionService.findActiveSessions();

        var availableComputers = computers.stream()
              .filter(c -> c.getComputerStatus() == ComputerStatus.FREE)
              .toList();

        if (availableComputers.isEmpty()) {
            System.out.println("Немає доступних комп'ютерів.");
            pause();
            return;
        }

        System.out.println("Доступні комп'ютери:");
        for (int i = 0; i < availableComputers.size(); i++) {
            Computer c = availableComputers.get(i);
            System.out.printf("%d. %s\n", i + 1, formatComputer(c));
        }

        System.out.println();
        int computerChoice = readInt("Оберіть комп'ютер (0 - скасувати): ");

        if (computerChoice < 1 || computerChoice > availableComputers.size()) {
            System.out.println("Скасовано.");
            pause();
            return;
        }

        Computer selectedComputer = availableComputers.get(computerChoice - 1);

        System.out.println();
        String name = readLine("Введіть nickname користувача: ");

        if (name.trim().isEmpty()) {
            System.out.println("Скасовано.");
            pause();
        }

        Client selectedClient = clientService.findByName(name).getFirst();

        System.out.println();
        List<Tariff> tariffs = tariffService.getAllTariffs();

        for (int i = 0; i < tariffs.size(); i++) {
            System.out.println((i + 1) + ". ");
            printTariff(tariffs.get(i));
        }

        int tariffChoice = readInt("Оберіть номер тарифу: ");
        Tariff selectedTariff = null;

        if (tariffChoice > 0 && tariffChoice <= tariffs.size()) {
            selectedTariff = tariffs.get(tariffChoice - 1);
        } else {
            System.out.println("Невірний вибір.");
            pause();
        }

        System.out.println();
        String confirm = readLine("Розпочати сесію? (yes/no): ");

        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Скасовано.");
            pause();
            return;
        }

        sessionService.startSession(selectedClient.getId(), selectedComputer.getId(),
              selectedTariff.getId());
        pause();
    }

    private void viewComputerHall() {
        clearScreen();
        printHeader("КОМП'ЮТЕРНИЙ ЗАЛ");

        var computers = computerService.getAllComputers();

        if (computers.isEmpty()) {
            System.out.println("Комп'ютерів ще немає.");
        } else {
            System.out.println("Всього комп'ютерів: " + computers.size());
            System.out.println();
        }

        System.out.println("=== ВІЛЬНІ ===");
        computers.stream()
              .filter(c -> c.getComputerStatus() == ComputerStatus.FREE)
              .forEach(c -> System.out.println("  " + formatComputer(c)));

        System.out.println();
        System.out.println("=== ЗАЙНЯТІ ===");
        computers.stream()
              .filter(c -> c.getComputerStatus() == ComputerStatus.BUSY)
              .forEach(c -> System.out.println("  " + formatComputer(c)));

        System.out.println();
        System.out.println("=== НА ОБСЛУГОВУВАННІ ===");
        computers.stream()
              .filter(c -> c.getComputerStatus() == ComputerStatus.OFFLINE)
              .forEach(c -> System.out.println("  " + formatComputer(c)));
        pause();
    }

    private String formatComputer(Computer c) {
        return String.format("ПК #%-3d [%s] [%s] ID: %s",
              c.getNumber(),
              c.getComputerType() == ComputerType.STANDART ? "СТАНДАРТ" : "VIP     ",
              formatStatus(c.getComputerStatus()),
              c.getId().toString().substring(0, 8));
    }

    private String formatStatus(ComputerStatus status) {
        return switch (status) {
            case FREE -> "ВІЛЬНИЙ  ";
            case BUSY -> "ЗАЙНЯТИЙ ";
            case OFFLINE -> "ВІДКЛЮЧЕН";
        };
    }

    private void addComputer() {
        clearScreen();
        printHeader("ДОДАТИ КОМП'ЮТЕР");

        int number = readInt("Номер комп'ютера: ");

        System.out.println();
        System.out.println("Тип комп'ютера:");
        System.out.println("1. Стандартний");
        System.out.println("2. VIP");
        int typeChoice = readInt("Оберіть тип: ");

        ComputerType type = typeChoice == 2 ? ComputerType.VIP : ComputerType.STANDART;

        computerService.createComputer(number, type);
        System.out.println();
        pause();
    }

    private void deleteComputer() {
        clearScreen();
        printHeader("ВИДАЛИТИ КОМП'ЮТЕР");

        var computers = computerService.getAllComputers();
        if (computers.isEmpty()) {
            System.out.println("Комп'ютерів немає.");
            pause();
            return;
        }

        System.out.println("Список комп'ютерів:");
        for (int i = 0; i < computers.size(); i++) {
            Computer c = computers.get(i);
            System.out.printf("%d. %s\n", i + 1, formatComputer(c));
        }

        System.out.println();
        int choice = readInt("Оберіть комп'ютер (0 - скасувати): ");

        if (choice < 1 || choice > computers.size()) {
            System.out.println("Скасовано.");
            pause();
            return;
        }

        Computer selected = computers.get(choice - 1);

        String confirm = readLine("Видалити ПК #" + selected.getNumber() + "? (yes/no): ");
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Скасовано.");
            pause();
        }
        computerService.deleteComputer(selected.getId());
        System.out.println("Комп'ютер під номером " + selected.getNumber() + " успішно видалено.");
        pause();
    }

    private void viewActiveSessions() {
        clearScreen();
        printHeader("АКТИВНІ СЕСІЇ");

        var sessions = sessionService.findActiveSessions();

        if (sessions.isEmpty()) {
            System.out.println("Немає активних сесій.");
        } else {
            System.out.println("Активних сесій: " + sessions.size());
            System.out.println();

            for (Session session : sessions) {
                printSession(session);
                System.out.println();
            }
        }
        pause();
    }

    private void viewSessionsByPeriod() {
        clearScreen();
        printHeader("СЕСІЇ ЗА ПЕРІОД");

        System.out.println("Оберіть період:");
        System.out.println("1. Сьогодні");
        System.out.println("2. Вчора");
        System.out.println("3. За останні 7 днів");
        System.out.println("4. За останні 30 днів");
        System.out.println("5. Свій період");

        int choice = readInt("Оберіть: ");

        LocalDateTime start = null, end = null;

        switch (choice) {
            case 1 -> {
                start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                end = LocalDateTime.now();
            }
            case 2 -> {
                start = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
                end = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59);
            }
            case 3 -> {
                start = LocalDateTime.now().minusDays(7);
                end = LocalDateTime.now();
            }
            case 4 -> {
                start = LocalDateTime.now().minusDays(30);
                end = LocalDateTime.now();
            }
            default -> {
                System.out.println("Скасовано.");
                pause();
            }
        }

        var sessions = sessionService.findSessionsBetween(start, end);

        System.out.println();
        System.out.println("Знайдено сесій: " + sessions.size());
        System.out.println();

        if (!sessions.isEmpty()) {
            for (Session session : sessions) {
                printSession(session);
                System.out.println();
            }
        }

        pause();
    }

    private void forceEndSession() {
        clearScreen();
        printHeader("ПРИМУСОВЕ ЗАВЕРШЕННЯ СЕСІЇ");

        var sessions = sessionService.findActiveSessions();

        if (sessions.isEmpty()) {
            System.out.println("Немає активних сесій.");
            pause();
            return;
        }

        System.out.println("Активні сесії:");
        for (int i = 0; i < sessions.size(); i++) {
            Session s = sessions.get(i);
            System.out.printf("%d. Клієнт: %s, ПК: %s\n",
                  i + 1,
                  s.getClientId().toString(),
                  s.getComputerId().toString());
        }

        System.out.println();
        int choice = readInt("Оберіть сесію (0 - скасувати): ");

        if (choice < 1 || choice > sessions.size()) {
            System.out.println("Скасовано.");
            pause();
            return;
        }

        Session selected = sessions.get(choice - 1);

        String confirm = readLine("Завершити сесію? (yes/no): ");
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Скасовано.");
            pause();
            return;
        }

        sessionService.endSession(selected.getId());
        System.out.println();
        pause();
    }

    private void viewStatistics() {
        clearScreen();
        printHeader("СТАТИСТИКА ПЛАТЕЖІВ");

        System.out.println("Оберіть період:");
        System.out.println("1. Сьогодні");
        System.out.println("2. За останні 7 днів");
        System.out.println("3. За останні 30 днів");

        int choice = readInt("Оберіть: ");

        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        switch (choice) {
            case 1 -> start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            case 2 -> start = LocalDateTime.now().minusDays(7);
            case 3 -> start = LocalDateTime.now().minusDays(30);
            default -> {
                System.out.println("Скасовано.");
                pause();
                return;
            }
        }

        var paymentStats = paymentService.getStatistics(start, end);

        System.out.println();
        System.out.println("=== ПЛАТЕЖІ ===");
        System.out.println("Всього платежів: " + paymentStats.totalPayments());
        System.out.println("Загальна сума: " + paymentStats.totalAmount() + " грн");
        System.out.println("Готівкою: " + paymentStats.cashAmount() + " грн");
        System.out.println("Карткою: " + paymentStats.cardAmount() + " грн");

        pause();
    }

    private void printSession(Session session) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        System.out.println("ID: " + session.getId().toString().substring(0, 8));
        System.out.println("Клієнт ID: " + session.getClientId().toString());
        System.out.println("Комп'ютер ID: " + session.getComputerId().toString());
        System.out.println("Початок: " + session.getStartTime().format(formatter));

        if (session.getEndTime() != null) {
            System.out.println("Завершення: " + session.getEndTime().format(formatter));
            System.out.println("Вартість: " + session.getTotalCost() + " грн");
        } else {
            if (session.isActive()) {
                System.out.println("Статус: АКТИВНА");
            } else {
                System.out.println("Статус: НЕАКТИВНА");
            }
        }
    }

    private void printTariff(Tariff tariff) {
        System.out.println("─".repeat(50));
        System.out.println("Назва: " + tariff.getName());
        System.out.println("Ціна: " + tariff.getPricePerHour() + " грн/год");
        System.out.println("Час дії: " + tariff.getStartHour() + " - " + tariff.getEndHour());
        System.out.println("Нічний: " + (tariff.isNightTariff() ? "Так" : "Ні"));
        System.out.println("─".repeat(50));
    }
}
