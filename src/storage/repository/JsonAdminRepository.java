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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import models.entities.Admin;
import storage.contracts.AdminRepository;
import storage.util.UUIDAdapter;

public class JsonAdminRepository implements AdminRepository {

    private final String filePath;

    private final Gson gson;

    public JsonAdminRepository(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting()
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

    private void writeToFile(List<Admin> admins) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(admins, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка запису в файл: " + filePath, e);
        }
    }

    private List<Admin> readFromFile() {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<Admin>>() {
            }.getType();
            List<Admin> admins = gson.fromJson(reader, listType);
            return admins != null ? admins : new ArrayList<>();
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка читання з файлу: " + filePath, e);
        }
    }

    @Override
    public Admin save(Admin entity) {
        List<Admin> admins = readFromFile();
        admins.add(entity);
        writeToFile(admins);
        return entity;
    }

    @Override
    public Admin update(Admin entity) {
        List<Admin> admins = readFromFile();
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getId().equals(entity.getId())) {
                admins.set(i, entity);
                writeToFile(admins);
                return entity;
            }
        }

        throw new IllegalArgumentException("Адміна з ID " + entity.getId() + " не знайдено");
    }

    @Override
    public void delete(UUID id) {
        List<Admin> admins = readFromFile();
        admins.removeIf(admin -> admin.getId().equals(id));
        writeToFile(admins);
    }

    @Override
    public Optional<Admin> findById(UUID id) {
        return readFromFile().stream()
              .filter(admin -> admin.getId().equals(id))
              .findFirst();
    }

    @Override
    public List<Admin> findAll() {
        return readFromFile();
    }

    @Override
    public Optional<Admin> findByLogin(String login) {
        return readFromFile().stream()
              .filter(admin -> admin.getLogin().equals(login))
              .findFirst();
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        return readFromFile().stream()
              .filter(admin -> admin.getEmail().equals(email))
              .findFirst();
    }
}
