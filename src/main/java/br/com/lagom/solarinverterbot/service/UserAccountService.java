package br.com.lagom.solarinverterbot.service;

import br.com.lagom.solarinverterbot.model.UserAccount;
import br.com.lagom.solarinverterbot.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAccountService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    public UserAccount findById(Long id){
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    public UserAccount findByIdAndCompanyId(Long userId, Long companyId){
        return userAccountRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }
}
