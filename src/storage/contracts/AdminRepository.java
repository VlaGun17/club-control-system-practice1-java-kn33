package storage.contracts;

import java.util.Optional;
import models.entities.Admin;

public interface AdminRepository extends Repository<Admin> {

    Optional<Admin> findByLogin(String login);

    Optional<Admin> findByEmail(String email);
}
