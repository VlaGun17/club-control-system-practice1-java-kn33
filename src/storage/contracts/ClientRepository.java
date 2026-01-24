package repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import models.entities.Client;
import repository.util.Repository;

public interface ClientRepository extends Repository<Client> {

    Optional<Client> findByEmail(String email);

    List<Client> findByNameContaining(String name);

    List<Client> findByRegistrationDate(LocalDateTime registrationDate);
}
