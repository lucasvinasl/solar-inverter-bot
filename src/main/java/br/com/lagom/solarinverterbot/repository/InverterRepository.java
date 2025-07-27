package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.Inverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InverterRepository extends JpaRepository<Inverter, Long> {

    @Query("""
    SELECT i FROM Inverter i WHERE i.serialNumber = :serialNumber AND i.plant.id = :plantId
    """)
    Optional<Inverter> findBySerialNumberAndPlantId(String serialNumber, Long plantId);
}
