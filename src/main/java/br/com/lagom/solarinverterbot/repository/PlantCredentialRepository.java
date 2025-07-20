package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.PlantCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlantCredentialRepository extends JpaRepository<PlantCredential, Long> {

    @Query("""
    SELECT pc FROM PlantCredential pc WHERE pc.username = :username
    """)
    Optional<PlantCredential> findByUsername(String username);
}
