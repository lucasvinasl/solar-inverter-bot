package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("""
    select distinct c
        from Client c
            join c.plants p
                join p.inverterList i
                    where i.inverterManufacturer.id = :manufacturerId
    """)
    List<Client> findAllByManufacturerId(Long manufacturerId);


    @Query("""
    select c
        from Client c
            join c.plants p
                join p.inverterManufacturer im
                    where im.id = :manufacturerId
                        and c.id = :clientId

    """)
    Optional<Client> findByIdAndManufacturerId(Long clientId, Long manufacturerId);

    Optional<Client> findByNameIgnoreCase(String name);

}
