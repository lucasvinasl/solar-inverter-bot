package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.Inverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InverterRepository extends JpaRepository<Inverter, Long> {
}
