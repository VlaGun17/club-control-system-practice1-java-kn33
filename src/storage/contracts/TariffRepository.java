package storage.contracts;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import models.entities.Tariff;

public interface TariffRepository extends Repository<Tariff> {

    Optional<Tariff> findCurrentTariff(LocalDateTime now);

    List<Tariff> findNightTariffs();

    Optional<Tariff> findByName(String name);
}
