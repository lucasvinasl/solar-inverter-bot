package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.InverterManufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InverterManufacturerRepository extends JpaRepository<InverterManufacturer, Long> {

    @Query("""
    select im from InverterManufacturer im where im.name ilike :name
    """)
    Optional<InverterManufacturer> findByNameIgnoreCase(String name);

}
