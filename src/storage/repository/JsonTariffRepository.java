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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import models.entities.Tariff;
import storage.contracts.TariffRepository;
import storage.util.LocalTimeAdapter;
import storage.util.UUIDAdapter;

public class JsonTariffRepository implements TariffRepository {

    private final String filePath;

    private final Gson gson;

    public JsonTariffRepository(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting()
              .registerTypeAdapter(UUID.class,
                    new UUIDAdapter())
              .registerTypeAdapter(LocalTime.class,
                    new LocalTimeAdapter()).create();
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

    private void writeToFile(List<Tariff> tariffs) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(tariffs, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка запису в файл: " + filePath, e);
        }
    }

    private List<Tariff> readFromFile() {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<Tariff>>() {
            }.getType();
            List<Tariff> tariffs = gson.fromJson(reader, listType);
            return tariffs != null ? tariffs : new ArrayList<>();
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка читання з файлу: " + filePath, e);
        }
    }

    @Override
    public Optional<Tariff> findCurrentTariff(LocalDateTime now) {
        LocalTime currentHour = now.toLocalTime();

        return findAll().stream().filter(tariff -> {
                  if (tariff.getStartHour().isBefore(tariff.getEndHour())) {
                      return currentHour.isAfter(tariff.getStartHour())
                            && currentHour.isBefore(tariff.getEndHour());
                  } else {
                      return currentHour.isAfter(tariff.getStartHour()) || currentHour.isBefore(
                            tariff.getEndHour());
                  }
              })
              .findFirst();
    }

    @Override
    public List<Tariff> findNightTariffs() {
        return findAll().stream()
              .filter(Tariff::isNightTariff)
              .toList();
    }

    @Override
    public Tariff save(Tariff entity) {
        List<Tariff> tariffs = readFromFile();
        tariffs.add(entity);
        writeToFile(tariffs);
        return entity;
    }

    @Override
    public Tariff update(Tariff entity) {
        List<Tariff> tariffs = readFromFile();
        for (int i = 0; i < tariffs.size(); i++) {
            if (tariffs.get(i).getId().equals(entity.getId())) {
                tariffs.set(i, entity);
                writeToFile(tariffs);
                return entity;
            }
        }

        throw new IllegalArgumentException("Тариф з ID " + entity.getId() + " не знайдено");
    }

    @Override
    public void delete(UUID id) {
        List<Tariff> tariffs = readFromFile();
        tariffs.removeIf(tariff -> tariff.getId().equals(id));
        writeToFile(tariffs);
    }

    @Override
    public Optional<Tariff> findById(UUID id) {
        return findAll().stream()
              .filter(tariff -> tariff.getId().equals(id))
              .findFirst();
    }

    @Override
    public Optional<Tariff> findByName(String name) {
        return findAll().stream()
              .filter(tariff -> tariff.getName().equals(name))
              .findFirst();
    }

    @Override
    public List<Tariff> findAll() {
        return readFromFile();
    }
}
