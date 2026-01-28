package services;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import models.entities.Client;
import models.entities.Payment;
import models.entities.Session;
import models.entities.Tariff;
import models.enums.PaymentType;
import storage.contracts.ClientRepository;
import storage.contracts.ComputerRepository;
import storage.contracts.PaymentRepository;
import storage.contracts.SessionRepository;
import storage.contracts.TariffRepository;
import storage.uow.UnitOfWork;
import validation.SessionValidator;
import validation.ValidationResult;

public class SessionService {

    private final SessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;
    private final ClientService clientService;
    private final ComputerService computerService;
    private final TariffService tariffService;
    private final SessionValidator sessionValidator;
    private final UnitOfWork<Session, UUID> uow;
    private final UnitOfWork<Payment, UUID> uowPayment;

    public SessionService(SessionRepository sessionRepository,
          ClientService clientService,
          ComputerService computerService,
          TariffService tariffService,
          ClientRepository clientRepository,
          ComputerRepository computerRepository,
          TariffRepository tariffRepository,
          PaymentRepository paymentRepository) {
        this.sessionRepository = sessionRepository;
        this.paymentRepository = paymentRepository;
        this.clientService = clientService;
        this.computerService = computerService;
        this.tariffService = tariffService;
        this.sessionValidator = new SessionValidator(sessionRepository, clientRepository,
              computerRepository, tariffRepository);
        this.uow = new UnitOfWork<>(sessionRepository, Session::getId);
        this.uowPayment = new UnitOfWork<>(paymentRepository, Payment::getId);
    }

    public void startSession(UUID clientId, UUID computerId, UUID tariffId) {
        Session newSession = new Session(clientId, computerId, tariffId);

        ValidationResult result = sessionValidator.validate(newSession);

        if (result.isValid()) {
            computerService.occupyComputer(computerId);
            uow.registerNew(newSession);
            uow.commit();
            System.out.println("✓ Сесію розпочато");
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public void endSession(UUID sessionId) {
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("Сесію не знайдено");
        }

        Session session = sessionOpt.get();

        if (session.getEndTime() != null) {
            throw new IllegalArgumentException("Сесія вже завершена");
        }

        LocalDateTime endTime = LocalDateTime.now();

        long minutes = Duration.between(session.getStartTime(), endTime)
              .toMinutes();
        if (minutes < 1) {
            minutes = 1;
        }

        Tariff tariff = getTariff(session.getTariffId());
        Client client = getClient(session.getClientId());

        BigDecimal totalCost = tariffService.calculateCost(tariff, minutes,
              client.getDiscountPercent());

        Session completedSession = new Session(session.getId(), session.getClientId(),
              session.getComputerId(), session.getTariffId(), session.getStartTime(),
              endTime, totalCost, false);

        ValidationResult result = sessionValidator.validate(completedSession);
        clientService.deductBalance(session.getClientId(), totalCost);
        computerService.freeComputer(session.getComputerId());

        Payment payment = new Payment(completedSession.getId(), totalCost, PaymentType.CASH);

        if (result.isValid()) {
            uow.registerDirty(completedSession);
            uowPayment.registerNew(payment);
            uow.commit();
            uowPayment.commit();
            clientService.registerVisit(session.getClientId());
            System.out.println(
                  "✓ Сесію завершено. Тривалість : " + minutes + " хв. Вартість: " + totalCost
                        + " грн");
        } else {
            System.out.println("✗ Помилки валідації:");
            System.out.println(result.getErrorMessage());
        }
    }

    public void forceEndSession(UUID sessionId,
          UUID adminId) {

        System.out.println("Адміністратор " + adminId +
              " примусово завершує сесію " + sessionId);

        endSession(sessionId);
    }

    public Optional<Session> findActiveSessionByClient(UUID clientId) {
        return sessionRepository.findAll().stream()
              .filter(s -> s.getClientId().equals(clientId) &&
                    s.getEndTime() == null)
              .findFirst();
    }

    public Optional<Session> findActiveSessionByComputer(UUID computerId) {
        return sessionRepository.findAll().stream()
              .filter(s -> s.getComputerId().equals(computerId) &&
                    s.getEndTime() == null)
              .findFirst();
    }

    public List<Session> findActiveSessions() {
        return sessionRepository.findAll().stream()
              .filter(session -> session.isActive())
              .toList();
    }

    public List<Session> findSessionsByClient(String clientId) {
        return sessionRepository.findAll().stream()
              .filter(s -> s.getClientId().equals(clientId))
              .toList();
    }

    public List<Session> findSessionsByComputer(String computerId) {
        return sessionRepository.findAll().stream()
              .filter(s -> s.getComputerId().equals(computerId))
              .toList();
    }

    public List<Session> findSessionsBetween(LocalDateTime start,
          LocalDateTime end) {
        return sessionRepository.findAll().stream()
              .filter(s -> !s.getStartTime().isBefore(start) &&
                    !s.getStartTime().isAfter(end))
              .toList();
    }

    private Tariff getTariff(UUID tariffId) {
        return tariffService.getAllTariffs().stream()
              .filter(t -> t.getId().equals(tariffId))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("Тариф не знайдено"));
    }

    private Client getClient(UUID clientId) {
        return clientService.getAllClients().stream()
              .filter(c -> c.getId().equals(clientId))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("Клієнта не знайдено"));
    }
}
