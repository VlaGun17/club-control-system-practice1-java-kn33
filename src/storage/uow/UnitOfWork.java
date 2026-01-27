package storage.uow;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import storage.contracts.Repository;

public class UnitOfWork<T, ID> {

    private final Set<T> newEntities = new LinkedHashSet<>();
    private final Set<T> dirtyEntities = new LinkedHashSet<>();
    private final Set<ID> deletedIds = new LinkedHashSet<>();

    private final Function<T, ID> idExtractor;
    private final Repository<T> repository;

    public UnitOfWork(Repository<T> repository, Function<T, ID> idExtractor) {
        this.repository = repository;
        this.idExtractor = idExtractor;
    }

    public void registerNew(T entity) {
        ID id = idExtractor.apply(entity);
        deletedIds.remove(id);
        dirtyEntities.remove(entity);
        newEntities.add(entity);
    }

    public void registerDirty(T entity) {
        ID id = idExtractor.apply(entity);
        if (!newEntities.contains(entity) && !deletedIds.contains(id)) {
            dirtyEntities.add(entity);
        }
    }

    public void registerDeleted(T entity) {
        ID id = idExtractor.apply(entity);
        if (newEntities.remove(entity)) {
            return;
        }
        dirtyEntities.remove(entity);
        deletedIds.add(id);
    }

    public void commit() {
        try {
            for (T entity : newEntities) {
                repository.save(entity);
            }

            for (T entity : dirtyEntities) {
                repository.update(entity);
            }

            for (ID id : deletedIds) {
                repository.delete((UUID) id);
            }

            clear();
        } catch (Exception e) {
            rollback();
            throw new RuntimeException("Помилка при збереженні змін", e);
        }
    }

    public void rollback() {
        clear();
    }

    public void clear() {
        newEntities.clear();
        dirtyEntities.clear();
        deletedIds.clear();
    }

    public boolean hasChanges() {
        return !newEntities.isEmpty() || !dirtyEntities.isEmpty() || !deletedIds.isEmpty();
    }

    public String getChangesSummary() {
        return String.format(
              "New: %d, Dirty: %d, Deleted: %d",
              newEntities.size(),
              dirtyEntities.size(),
              deletedIds.size()
        );
    }
}
