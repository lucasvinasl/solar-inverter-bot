package br.com.lagom.solarinverterbot.async;

import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    @Query("""
    select qe from QueueEntry qe where qe.statusQueue = 'IN_PROGRESS' and qe.creator.id = :userId
    """)
    List<QueueEntry> findAllByUserAccountId(Long userId);

    @Query("""
        select count(qe) > 0
        from QueueEntry qe
        where qe.statusQueue = 'IN_PROGRESS'
        and qe.creator.id = :userId
    """)
    boolean isRunningQueueByUser(Long userId);

    @Query("""
        select count(qe) > 0
        from QueueEntry qe
        where qe.statusQueue IN('IN_PROGESS','PENDING')
        and qe.creator.company.id = :companyId
        and qe.inverterManufacturer.id = :manufacturerId
    """)
    boolean isOpenByCompanyIdAndManufacturerId(Long companyId, Long manufacturerId);

    List<QueueEntry> findAllByStatusQueue(StatusQueueEnum statusQueue);

    @Query("""
    select qe from QueueEntry qe where qe.statusQueue in('IN_PROGESS','PENDING') and qe.creator.company.id = :companyId
    """)
    List<QueueEntry> findAllOpenByCompanyId(Long companyId);

}
