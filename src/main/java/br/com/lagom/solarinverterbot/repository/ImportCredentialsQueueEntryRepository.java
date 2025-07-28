package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.ImportCredentialsQueueEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface ImportCredentialsQueueEntryRepository extends JpaRepository<ImportCredentialsQueueEntry, Long> {

    @Query(value = """
    select * from import_credentials_queue_entry ic where ic.status_credentials = 'PENDING' and ic.created_at > :timoutLimit
    FOR UPDATE SKIP LOCKED LIMIT 1
    """, nativeQuery = true)
    List<ImportCredentialsQueueEntry> findTop1Pending(ZonedDateTime timoutLimit);


    Page<ImportCredentialsQueueEntry> findAllByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);


}
