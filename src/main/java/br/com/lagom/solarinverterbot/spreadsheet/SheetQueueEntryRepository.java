package br.com.lagom.solarinverterbot.spreadsheet;

import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SheetQueueEntryRepository extends JpaRepository<SheetQueueEntry, Long> {
    List<SheetQueueEntry> findTop10ByStatusSheetOrderByCreatedAtAsc(StatusQueueEnum statusQueueEnum);
}
