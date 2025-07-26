package br.com.lagom.solarinverterbot.async;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantQueueEntryRepository extends JpaRepository<PlantQueueEntry, Long> {
    List<PlantQueueEntry> findByQueueEntry(QueueEntry queueEntry);
}
