package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import br.com.lagom.solarinverterbot.model.SheetGenerationDataQueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SheetGenerationDataQueueEntryRepository extends JpaRepository<SheetGenerationDataQueueEntry, Long> {
    List<SheetGenerationDataQueueEntry> findTop10ByStatusSheetOrderByCreatedAtAsc(StatusQueueEnum statusQueueEnum);
}
