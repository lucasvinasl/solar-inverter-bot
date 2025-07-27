package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.MonthlyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyDataRepository extends JpaRepository<MonthlyData, Long> {

    @Query("""
    SELECT energy FROM MonthlyData energy WHERE energy.year = :year
        AND energy.inverter = :inverterId
    """)
    Optional<MonthlyData> findByYearAndInverterId(Integer year, Long inverterId);
}
