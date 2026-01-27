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
import models.entities.Client;
import storage.contracts.ClientRepository;
import storage.util.LocalDateTimeAdapter;
import storage.util.UUIDAdapter;

public class JsonClientRepository implements ClientRepository {

    private final String filePath;

    private final Gson gson;

    public JsonClientRepository(String filePath) {
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

    private void writeToFile(List<Client> clients) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(clients, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка запису в файл: " + filePath, e);
        }
    }

    private List<Client> readFromFile() {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<Client>>() {
            }.getType();
            List<Client> clients = gson.fromJson(reader, listType);
            return clients != null ? clients : new ArrayList<>();
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка читання з файлу: " + filePath, e);
        }
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        return readFromFile().stream().
              filter(client -> client.getEmail().equals(email))
              .findFirst();
    }

    @Override
    public List<Client> findByNameContaining(String name) {
        return readFromFile().stream()
              .filter(client -> client.getNickname().toLowerCase()
                    .contains(name.toLowerCase()))
              .collect(Collectors.toList());
    }

    @Override
    public List<Client> findByRegistrationDate(LocalDateTime registrationDate) {
        return readFromFile().stream()
              .filter(client -> client.getRegistrationDate()
                    .equals(registrationDate))
              .collect(Collectors.toList());
    }

    @Override
    public Client save(Client entity) {
        List<Client> clients = readFromFile();
        clients.add(entity);
        writeToFile(clients);
        return entity;
    }

    @Override
    public Client update(Client entity) {
        List<Client> clients = readFromFile();
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getId().equals(entity.getId())) {
                clients.set(i, entity);
                writeToFile(clients);
                return entity;
            }
        }

        throw new IllegalArgumentException("Користувача з ID " + entity.getId() + " не знайдено");
    }

    @Override
    public void delete(UUID id) {
        List<Client> clients = readFromFile();
        clients.removeIf(client -> client.getId().equals(id));
        writeToFile(clients);
    }

    @Override
    public Optional<Client> findById(UUID id) {
        return readFromFile().stream().filter(client -> client.getId()
              .equals(id)).findFirst();
    }

    @Override
    public List<Client> findAll() {
        return readFromFile();
    }
}
