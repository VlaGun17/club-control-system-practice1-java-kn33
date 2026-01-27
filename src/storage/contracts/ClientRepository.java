package storage.contracts;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import models.entities.Client;

public interface ClientRepository extends Repository<Client> {

    Optional<Client> findByEmail(String email);

    List<Client> findByNameContaining(String name);

    List<Client> findByRegistrationDate(LocalDateTime registrationDate);
}
