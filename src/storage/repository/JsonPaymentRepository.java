package storage.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import models.entities.Payment;
import models.enums.PaymentType;
import storage.contracts.PaymentRepository;
import storage.util.LocalDateTimeAdapter;
import storage.util.UUIDAdapter;

public class JsonPaymentRepository implements PaymentRepository {

    private final String filePath;

    private final Gson gson;

    public JsonPaymentRepository(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting()
              .registerTypeAdapter(LocalDateTime.class
                    , new LocalDateTimeAdapter())
              .registerTypeAdapter(UUID.class,
                    new UUIDAdapter()).create();
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("[]");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Не вдалося створити файл: " + filePath, e);
            }
        }
    }

    private void writeToFile(List<Payment> payments) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(payments, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка запису в файл: " + filePath, e);
        }
    }

    private List<Payment> readFromFile() {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<Payment>>() {
            }.getType();
            List<Payment> payments = gson.fromJson(reader, listType);
            return payments != null ? payments : new ArrayList<>();
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка читання з файлу: " + filePath, e);
        }
    }

    @Override
    public Optional<Payment> findBySessionId(UUID sessionId) {
        return readFromFile().stream()
              .filter(payment -> payment.getSessionId().equals(sessionId))
              .findFirst();
    }

    @Override
    public BigDecimal getTotalRevenue(LocalDateTime date) {
        return findAll().stream()
              .filter(payment -> payment.getPaymentTime().toLocalDate().isEqual(date.toLocalDate()))
              .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Payment> findByType(PaymentType type) {
        return findAll().stream()
              .filter(payment -> payment.getPaymentType().equals(type))
              .collect(Collectors.toList());
    }

    @Override
    public Payment save(Payment entity) {
        List<Payment> payments = readFromFile();
        payments.add(entity);
        writeToFile(payments);
        return entity;
    }

    @Override
    public Payment update(Payment entity) {
        List<Payment> payments = readFromFile();
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getId().equals(entity.getId())) {
                payments.set(i, entity);
                writeToFile(payments);
                return entity;
            }
        }

        throw new IllegalArgumentException("Платіж з ID " + entity.getId() + " не знайдено");
    }

    @Override
    public void delete(UUID id) {
        List<Payment> payments = readFromFile();
        payments.removeIf(payment -> payment.getId().equals(id));
        writeToFile(payments);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return findAll().stream()
              .filter(payment -> payment.getId().equals(id))
              .findFirst();
    }

    @Override
    public List<Payment> findAll() {
        return readFromFile();
    }
}
