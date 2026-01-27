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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import models.entities.Session;
import storage.contracts.SessionRepository;
import storage.util.LocalDateTimeAdapter;
import storage.util.UUIDAdapter;

public class JsonSessionRepository implements SessionRepository {

    private final String filePath;

    private final Gson gson;

    public JsonSessionRepository(String filePath) {
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

    private void writeToFile(List<Session> sessions) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(sessions, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка запису в файл: " + filePath, e);
        }
    }

    private List<Session> readFromFile() {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<Session>>() {
            }.getType();
            List<Session> sessions = gson.fromJson(reader, listType);
            return sessions != null ? sessions : new ArrayList<>();
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка читання з файлу: " + filePath, e);
        }
    }


    @Override
    public List<Session> findByClientId(UUID clientId) {
        return findAll().stream()
              .filter(session -> session.getClientId().equals(clientId)).collect(
                    Collectors.toList());
    }

    @Override
    public Session save(Session entity) {
        List<Session> sessions = readFromFile();
        sessions.add(entity);
        writeToFile(sessions);
        return entity;
    }

    @Override
    public Session update(Session entity) {
        List<Session> sessions = readFromFile();
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getId().equals(entity.getId())) {
                sessions.set(i, entity);
                writeToFile(sessions);
                return entity;
            }
        }

        throw new IllegalArgumentException("Сесію з ID " + entity.getId() + " не знайдено");
    }

    @Override
    public void delete(UUID id) {
        List<Session> sessions = readFromFile();
        sessions.removeIf(session -> session.getId().equals(id));
        writeToFile(sessions);
    }

    @Override
    public Optional<Session> findById(UUID id) {
        return findAll().stream()
              .filter(session -> session.getId().equals(id))
              .findFirst();
    }

    @Override
    public List<Session> findAll() {
        return readFromFile();
    }
}
