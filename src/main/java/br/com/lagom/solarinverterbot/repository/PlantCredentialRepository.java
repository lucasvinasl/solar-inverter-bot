package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.PlantCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantCredentialRepository extends JpaRepository<PlantCredential, Long> {
}
