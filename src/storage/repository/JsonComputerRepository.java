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
import java.util.stream.Collectors;
import models.entities.Computer;
import models.enums.ComputerStatus;
import models.enums.ComputerType;
import storage.contracts.ComputerRepository;
import storage.util.UUIDAdapter;

public class JsonComputerRepository implements ComputerRepository {

    private final String filePath;

    private final Gson gson;

    public JsonComputerRepository(String filePath) {
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

    private void writeToFile(List<Computer> computers) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(computers, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка запису в файл: " + filePath, e);
        }
    }

    private List<Computer> readFromFile() {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<Computer>>() {
            }.getType();
            List<Computer> computers = gson.fromJson(reader, listType);
            return computers != null ? computers : new ArrayList<>();
        } catch (IOException e) {
            throw new IllegalArgumentException("Помилка читання з файлу: " + filePath, e);
        }
    }

    @Override
    public List<Computer> findByComputerType(ComputerType computerType) {
        return readFromFile().stream()
              .filter(computer -> computer.getComputerType().equals(computerType))
              .collect(Collectors.toList());
    }

    @Override
    public List<Computer> findByComputerStatus(ComputerStatus computerStatus) {
        return readFromFile().stream()
              .filter(computer -> computer.getComputerStatus().equals(computerStatus))
              .collect(Collectors.toList());
    }

    @Override
    public Optional<Computer> findByNumber(int number) {
        return readFromFile().stream()
              .filter(computer -> computer.getNumber() == number)
              .findFirst();
    }

    @Override
    public Computer save(Computer entity) {
        List<Computer> computers = readFromFile();
        computers.add(entity);
        writeToFile(computers);
        return entity;
    }

    @Override
    public Computer update(Computer entity) {
        List<Computer> computers = readFromFile();
        for (int i = 0; i < computers.size(); i++) {
            if (computers.get(i).getId().equals(entity.getId())) {
                computers.set(i, entity);
                writeToFile(computers);
                return entity;
            }
        }

        throw new IllegalArgumentException("Комп'ютер з ID " + entity.getId() + " не знайдено");
    }

    @Override
    public void delete(UUID id) {
        List<Computer> computers = readFromFile();
        computers.removeIf(computer -> computer.getId().equals(id));
        writeToFile(computers);
    }

    @Override
    public Optional<Computer> findById(UUID id) {
        return readFromFile().stream().filter(computer -> computer.getId().equals(id)).findFirst();
    }

    @Override
    public List<Computer> findAll() {
        return readFromFile();
    }
}
