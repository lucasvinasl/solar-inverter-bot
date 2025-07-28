package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.PlantQueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantQueueEntryRepository extends JpaRepository<PlantQueueEntry, Long> {
}
