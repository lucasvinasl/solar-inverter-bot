package br.com.lagom.solarinverterbot.service;

import br.com.lagom.solarinverterbot.model.Plant;
import br.com.lagom.solarinverterbot.repository.PlantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantService {

    @Autowired
    private PlantRepository plantRepository;

    public List<Plant> findAll(){
        List<Plant> all =  plantRepository.findAll();
        if(all.isEmpty()){
            throw new EntityNotFoundException("Nenhum cliente cadastrado.");
        }
        return all;
    }

    public Plant findById(Long id){
        return plantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado."));
    }

    public List<Plant> findAllByManufacturerId(Long manufacturerId){
        List<Plant> plants = plantRepository.findAllByManufacturerId(manufacturerId);
        if(plants.isEmpty()){
            throw new EntityNotFoundException("Nenhum cliente encontrado para esse fabricante.");
        }
        return plants;
    }

    public List<Plant> findAllFromIdAndManufacturerId(Long manufacturerId, Long plantId){
        List<Plant> plants = plantRepository.findAllByManufacturerIdFromPlantId(manufacturerId, plantId);
        if(plants.isEmpty()){
            throw new EntityNotFoundException("Nenhum cliente encontrado para esse fabricante.");
        }
        return plants;
    }
}
