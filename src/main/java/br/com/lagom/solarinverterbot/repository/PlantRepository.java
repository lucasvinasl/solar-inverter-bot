package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {

    @Query("""
    select distinct p
        from Plant p
            where p.inverterManufacturer.id = :manufacturerId
    """)
    List<Plant> findAllByManufacturerId(Long manufacturerId);

    @Query("""
    select p from Plant p where p.inverterManufacturer.id = :manufacturerId and p.id >= :plantId
    """)
    List<Plant> findAllByManufacturerIdFromPlantId(Long manufacturerId, Long plantId);
}
