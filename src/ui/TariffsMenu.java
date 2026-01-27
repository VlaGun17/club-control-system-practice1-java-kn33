package ui;

import static ui.MainMenu.clearScreen;
import static ui.MainMenu.pause;
import static ui.MainMenu.printHeader;
import static ui.MainMenu.readInt;
import static ui.MainMenu.readLine;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import models.entities.Tariff;
import services.TariffService;

public class TariffsMenu {

    private final Scanner scanner;
    private final TariffService tariffService;

    public TariffsMenu(Scanner scanner, TariffService tariffService) {
        this.scanner = scanner;
        this.tariffService = tariffService;
    }

    public void show() {
        while (true) {
            clearScreen();
            printHeader("ТАРИФИ");
            System.out.println("1. Створити тариф");
            System.out.println("2. Переглянути тарифи");
            System.out.println("3. Видалити тариф");
            System.out.println("0. Назад");
            System.out.println();

            int choice = readInt("Оберіть пункт: ");

            switch (choice) {
                case 1 -> createTariff();
                case 2 -> viewTariffs();
                case 3 -> deleteTariff();
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

    private void createTariff() {
        clearScreen();
        printHeader("СТВОРИТИ ТАРИФ");

        String name = readLine("Назва тарифу: ");

        String priceStr = readLine("Ціна за годину (грн): ");
        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            System.out.println("✗ Некоректна ціна!");
            pause();
            return;
        }

        System.out.println();
        System.out.println("Час початку дії тарифу (формат ГГ:ХХ)");
        String startStr = readLine("Час початку: ");
        LocalTime startHour;
        try {
            startHour = LocalTime.parse(startStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            System.out.println("✗ Некоректний формат часу!");
            pause();
            return;
        }

        System.out.println("Час завершення дії тарифу (формат ГГ:ХХ)");
        String endStr = readLine("Час завершення: ");
        LocalTime endHour;
        try {
            endHour = LocalTime.parse(endStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            System.out.println("✗ Некоректний формат часу!");
            pause();
            return;
        }

        System.out.println();
        String nightStr = readLine("Нічний тариф? (yes/no): ");
        boolean isNightTariff = nightStr.equalsIgnoreCase("yes");

        tariffService.createTariff(
              name, price, startHour, endHour, isNightTariff);

        System.out.println();
        pause();
    }

    private void viewTariffs() {
        clearScreen();
        printHeader("СПИСОК ТАРИФІВ");

        var tariffs = tariffService.getAllTariffs();

        if (tariffs.isEmpty()) {
            System.out.println("Тарифів немає.");
        } else {
            System.out.println("Всього тарифів: " + tariffs.size());
            System.out.println();

            for (Tariff tariff : tariffs) {
                printTariff(tariff);
                System.out.println();
            }
        }

        pause();
    }

    private void deleteTariff() {
        clearScreen();
        printHeader("ВИДАЛИТИ ТАРИФ");

        var tariffs = tariffService.getAllTariffs();

        if (tariffs.isEmpty()) {
            System.out.println("Тарифів немає.");
            pause();
            return;
        }

        System.out.println("Список тарифів:");
        for (int i = 0; i < tariffs.size(); i++) {
            Tariff t = tariffs.get(i);
            System.out.printf("%d. %-20s %8s грн/год  %s - %s\n",
                  i + 1,
                  t.getName(),
                  t.getPricePerHour(),
                  t.getStartHour(),
                  t.getEndHour());
        }

        System.out.println();
        int choice = readInt("Оберіть тариф (0 - скасувати): ");

        if (choice < 1 || choice > tariffs.size()) {
            System.out.println("Скасовано.");
            pause();
            return;
        }

        Tariff selected = tariffs.get(choice - 1);

        String confirm = readLine("Видалити тариф '" + selected.getName() + "'? (yes/no): ");
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Скасовано.");
            pause();
        }

        tariffService.deleteTariff(selected.getId());
        System.out.println("Тариф " + selected.getName() + " видалено.");
        pause();
    }

    private void printTariff(Tariff tariff) {
        System.out.println("─".repeat(50));
        System.out.println("ID: " + tariff.getId().toString());
        System.out.println("Назва: " + tariff.getName());
        System.out.println("Ціна: " + tariff.getPricePerHour() + " грн/год");
        System.out.println("Час дії: " + tariff.getStartHour() + " - " + tariff.getEndHour());
        System.out.println("Нічний: " + (tariff.isNightTariff() ? "Так" : "Ні"));
        System.out.println("─".repeat(50));
    }
}
