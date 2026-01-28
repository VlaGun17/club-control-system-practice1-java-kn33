package ui;

import java.util.Scanner;
import models.entities.Admin;
import services.AdminService;
import services.AuthenticationService;
import services.ClientService;
import services.ComputerService;
import services.PaymentService;
import services.SessionService;
import services.TariffService;
import storage.contracts.AdminRepository;
import storage.contracts.ClientRepository;
import storage.contracts.ComputerRepository;
import storage.contracts.PaymentRepository;
import storage.contracts.SessionRepository;
import storage.contracts.TariffRepository;
import storage.repository.JsonAdminRepository;
import storage.repository.JsonClientRepository;
import storage.repository.JsonComputerRepository;
import storage.repository.JsonPaymentRepository;
import storage.repository.JsonSessionRepository;
import storage.repository.JsonTariffRepository;
import storage.util.PasswordHasher;

public class MainMenu {

    private final Scanner scanner;
    private final AuthenticationService authService;
    private final AdminService adminService;
    private final ClientService clientService;
    private final ComputerService computerService;
    private final TariffService tariffService;
    private final SessionService sessionService;
    private final PaymentService paymentService;

    private Admin currentAdmin;

    public MainMenu() {
        this.scanner = new Scanner(System.in);

        AdminRepository adminRepo = new JsonAdminRepository("data/admins.json");
        ClientRepository clientRepo = new JsonClientRepository("data/clients.json");
        ComputerRepository computerRepo = new JsonComputerRepository("data/computers.json");
        TariffRepository tariffRepo = new JsonTariffRepository("data/tariffs.json");
        SessionRepository sessionRepo = new JsonSessionRepository("data/sessions.json");
        PaymentRepository paymentRepo = new JsonPaymentRepository("data/payments.json");

        PasswordHasher passwordHasher = new PasswordHasher();
        this.adminService = new AdminService(adminRepo, passwordHasher);
        this.authService = new AuthenticationService(adminRepo, passwordHasher);
        this.clientService = new ClientService(clientRepo);
        this.computerService = new ComputerService(computerRepo);
        this.tariffService = new TariffService(tariffRepo);
        this.paymentService = new PaymentService(paymentRepo);
        this.sessionService = new SessionService(
              sessionRepo, clientService, computerService, tariffService, clientRepo, computerRepo,
              tariffRepo, paymentRepo);
    }

    public static void printHeader(String title) {
        System.out.println("═".repeat(70));
        System.out.println("   " + title);
        System.out.println("═".repeat(70));
        System.out.println();
    }

    public static int readInt(String prompt) {
        System.out.print(prompt);
        Scanner scanner = new Scanner(System.in);
        while (!scanner.hasNextInt()) {
            System.out.print("Введіть число: ");
            scanner.next();
        }
        return scanner.nextInt();
    }

    public static void pause() {
        System.out.println();
        System.out.println("Натисніть Enter для продовження...");
        try {
            System.in.read();
        } catch (Exception e) {
        }
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().trim();
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public Admin start() {
        printWelcomeBanner();

        AuthMenu authMenu = new AuthMenu(scanner, authService, adminService);
        try {
            currentAdmin = authMenu.show();

            if (currentAdmin == null) {
                System.out.println("До побачення!");
                return null;
            }

            showMainMenu();
        } catch (Exception e) {
            System.out.println();
            System.out.println("❌ Помилка: " + e.getMessage());
            pause();
            return null;
        }
        return null;
    }

    private void showMainMenu() {
        while (true) {
            clearScreen();
            printHeader("ГОЛОВНЕ МЕНЮ");
            System.out.println("Вітаємо, " + currentAdmin.getLogin() + "!");
            System.out.println();
            System.out.println("1. Система");
            System.out.println("2. Клієнти");
            System.out.println("3. Тарифи");
            System.out.println("4. Налаштування");
            System.out.println("0. Вихід");
            System.out.println();

            int choice = readInt("Оберіть пункт: ");

            switch (choice) {
                case 1 -> showSystemMenu();
                case 2 -> showClientsMenu();
                case 3 -> showTariffsMenu();
                case 4 -> showSettingsMenu();
                case 0 -> {
                    authService.logout();
                    return;
                }
                default -> System.out.println("Невірний вибір!");
            }
        }
    }

    private void showSystemMenu() {
        SystemMenu menu = new SystemMenu(
              scanner, computerService, sessionService, paymentService, clientService,
              tariffService);
        menu.show();
    }

    private void showClientsMenu() {
        ClientsMenu menu = new ClientsMenu(scanner, clientService);
        menu.show();
    }

    private void showTariffsMenu() {
        TariffsMenu menu = new TariffsMenu(scanner, tariffService);
        menu.show();
    }

    private void showSettingsMenu() {
        SettingsMenu menu = new SettingsMenu(
              scanner, adminService, authService, currentAdmin
        );

        if (!menu.show()) {
            System.out.println("Обліковий запис видалено. Вихід з системи...");
            currentAdmin = null;
            pause();
        }
    }

    private void printWelcomeBanner() {
        clearScreen();
        System.out.println("═".repeat(70));
        System.out.println("   СИСТЕМА АДМІНІСТРУВАННЯ КОМП'ЮТЕРНОГО КЛУБУ");
        System.out.println("═".repeat(70));
        System.out.println();
    }
}
