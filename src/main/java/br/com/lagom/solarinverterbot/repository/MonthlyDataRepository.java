package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.MonthlyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyDataRepository extends JpaRepository<MonthlyData, Long> {
}
