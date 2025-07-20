package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.InverterManufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InverterManufacturerRepository extends JpaRepository<InverterManufacturer, Long> {
}
