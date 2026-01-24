package Models.Entities;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseEntity implements Entity {

    private final UUID id;

    protected Map<String, List<String>> errors;

    protected BaseEntity() {
        this.id = UUID.randomUUID();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
