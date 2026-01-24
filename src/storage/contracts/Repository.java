package repository.util;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {

    T save(T entity);

    T update(T entity);

    void delete(String id);

    Optional<T> findById(String id);

    List<T> findAll();
}
