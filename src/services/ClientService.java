package services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import models.entities.Client;
import storage.contracts.ClientRepository;
import storage.uow.UnitOfWork;
import validation.ClientValidator;
import validation.ValidationResult;

public class ClientService {

    private static final int VISITS_FOR_5_PERCENT = 10;
    private static final int VISITS_FOR_10_PERCENT = 25;
    private static final int VISITS_FOR_15_PERCENT = 50;
    private static final int VISITS_FOR_20_PERCENT = 100;

    private final ClientRepository clientRepository;
    private final ClientValidator clientValidator;
    private final UnitOfWork<Client, UUID> uow;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
        this.uow = new UnitOfWork<>(clientRepository, Client::getId);
        this.clientValidator = new ClientValidator(clientRepository);
    }


    public Client createClient(String nickname, String email) {
        Client newClient = new Client(nickname, email, BigDecimal.ZERO, 0, BigDecimal.ZERO,
              LocalDateTime.now());

        ValidationResult result = clientValidator.validate(newClient);

        if (result.isValid()) {
            uow.registerNew(newClient);
            uow.commit();
            System.out.println("✓ Клієнта успішно створено.");
            return newClient;
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
            return null;
        }
    }

    public void addBalance(UUID clientId, BigDecimal amount) {

        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Клієнта не знайдено");
        }

        Client client = clientOpt.get();
        BigDecimal newBalance = client.getBalance().add(amount);
        client.setBalance(newBalance);

        ValidationResult result = clientValidator.validateAddBalance(client, amount);

        if (result.isValid()) {
            uow.registerDirty(client);
            uow.commit();
            System.out.println("Баланс поповнено на " + amount + " грн. Поточний баланс: " +
                  newBalance + " грн");
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public void deductBalance(UUID clientId, BigDecimal amount) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Клієнта не знайдено");
        }

        Client client = clientOpt.get();

        BigDecimal newBalance = client.getBalance().subtract(amount);

        ValidationResult result = clientValidator.validateDeduct(client, amount);

        client.setBalance(newBalance);

        if (result.isValid()) {
            uow.registerDirty(client);
            uow.commit();
            System.out.println("Списано " + amount + " грн. Залишок: " + newBalance + " грн");
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public void registerVisit(UUID clientId) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Клієнта не знайдено");
        }

        Client client = clientOpt.get();
        int newVisitCount = client.getVisitCount() + 1;

        BigDecimal newDiscount = calculateLoyaltyDiscount(newVisitCount);

        client.setVisitCount(newVisitCount);
        client.setDiscountPercent(newDiscount);

        ValidationResult result = clientValidator.validateRegisterVisit(client);

        String message = "Візит зареєстровано. Всього відвідувань: " + newVisitCount;
        if (newDiscount.compareTo(client.getDiscountPercent()) > 0) {
            message += ". Знижка збільшена до " + newDiscount + "%!";
        }

        if (result.isValid()) {
            uow.registerDirty(client);
            uow.commit();
            System.out.println(message);
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public void setCustomDiscount(UUID clientId, BigDecimal discountPercent) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Клієнта не знайдено");
        }

        Client client = clientOpt.get();
        client.setDiscountPercent(discountPercent);

        ValidationResult result = clientValidator.validate(client);

        if (result.isValid()) {
            uow.registerDirty(client);
            uow.commit();
            System.out.println("Знижка збільшена до " + discountPercent + "%.");
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public List<Client> findVipClients() {
        return clientRepository.findAll().stream()
              .filter(client -> client.getDiscountPercent()
                    .compareTo(new BigDecimal("15")) >= 0)
              .toList();
    }

    public List<Client> findNewClients(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return clientRepository.findAll().stream()
              .filter(client -> client.getRegistrationDate().isAfter(threshold))
              .toList();
    }

    public Optional<Client> findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    public List<Client> findByName(String name) {
        return clientRepository.findByNameContaining(name);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    private BigDecimal calculateLoyaltyDiscount(int visitCount) {
        if (visitCount >= VISITS_FOR_20_PERCENT) {
            return new BigDecimal("20");
        } else if (visitCount >= VISITS_FOR_15_PERCENT) {
            return new BigDecimal("15");
        } else if (visitCount >= VISITS_FOR_10_PERCENT) {
            return new BigDecimal("10");
        } else if (visitCount >= VISITS_FOR_5_PERCENT) {
            return new BigDecimal("5");
        }
        return BigDecimal.ZERO;
    }
}
