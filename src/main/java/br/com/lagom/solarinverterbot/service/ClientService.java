package br.com.lagom.solarinverterbot.service;

import br.com.lagom.solarinverterbot.model.Client;
import br.com.lagom.solarinverterbot.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public List<Client> findAll(){
        List<Client> all =  clientRepository.findAll();
        if(all.isEmpty()){
            throw new EntityNotFoundException("Nenhum cliente cadastrado.");
        }
        return all;
    }

    public Client findById(Long id){
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado."));
    }

    public List<Client> findAllByManufacturerId(Long manufacturerId){
        List<Client> clientList = clientRepository.findAllByManufacturerId(manufacturerId);
        if(clientList.isEmpty()){
            throw new EntityNotFoundException("Nenhum cliente encontrado para esse fabricante.");
        }
        return clientList;
    }

    public Client findByIdAndManufacturerId(Long clientId, Long manufacturerId){
        return clientRepository.findByIdAndManufacturerId(clientId,manufacturerId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrada para esse fabricante."));
    }
}
