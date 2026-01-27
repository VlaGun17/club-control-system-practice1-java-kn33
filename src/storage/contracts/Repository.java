package storage.contracts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Repository<T> {

    T save(T entity);

    T update(T entity);

    void delete(UUID id);

    Optional<T> findById(UUID id);

    List<T> findAll();
}
