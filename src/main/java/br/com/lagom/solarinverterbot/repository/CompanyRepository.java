package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
}
