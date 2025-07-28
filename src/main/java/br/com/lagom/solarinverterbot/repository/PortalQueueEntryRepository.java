package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import br.com.lagom.solarinverterbot.model.PortalQueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortalQueueEntryRepository extends JpaRepository<PortalQueueEntry, Long> {

    @Query("""
    select qe from PortalQueueEntry qe where qe.statusQueue = 'IN_PROGRESS' and qe.creator.id = :userId
    """)
    List<PortalQueueEntry> findAllByUserAccountId(Long userId);

    @Query("""
        select count(qe) > 0
        from PortalQueueEntry qe
        where qe.statusQueue = 'IN_PROGRESS'
        and qe.creator.id = :userId
    """)
    boolean isRunningQueueByUser(Long userId);

    @Query("""
        select count(qe) > 0
        from PortalQueueEntry qe
        where qe.statusQueue IN('IN_PROGESS','PENDING')
        and qe.creator.company.id = :companyId
        and qe.inverterManufacturer.id = :manufacturerId
    """)
    boolean isOpenByCompanyIdAndManufacturerId(Long companyId, Long manufacturerId);

    List<PortalQueueEntry> findAllByStatusQueue(StatusQueueEnum statusQueue);

    @Query("""
    select qe from PortalQueueEntry qe where qe.statusQueue in('IN_PROGESS','PENDING') and qe.creator.company.id = :companyId
    """)
    List<PortalQueueEntry> findAllOpenByCompanyId(Long companyId);

}
