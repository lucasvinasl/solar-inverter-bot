package br.com.lagom.solarinverterbot.repository;

import br.com.lagom.solarinverterbot.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    @Query("""
    select ua from UserAccount ua where ua.id = :id and ua.company.id = :companyId
    """)
    Optional<UserAccount> findByIdAndCompanyId(Long id, Long companyId);
}
