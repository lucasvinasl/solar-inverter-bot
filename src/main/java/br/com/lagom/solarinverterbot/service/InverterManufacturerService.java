package br.com.lagom.solarinverterbot.service;

import br.com.lagom.solarinverterbot.model.InverterManufacturer;
import br.com.lagom.solarinverterbot.repository.InverterManufacturerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InverterManufacturerService {

    @Autowired
    private InverterManufacturerRepository inverterManufacturerRepository;

    public InverterManufacturer findById(Long id){
        return inverterManufacturerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fabricante não encontrado."));
    }
}
