package storage.contracts;

import java.util.List;
import java.util.UUID;
import models.entities.Session;

public interface SessionRepository extends Repository<Session> {

    List<Session> findByClientId(UUID clientId);
}
