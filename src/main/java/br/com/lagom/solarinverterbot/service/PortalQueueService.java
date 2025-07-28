package br.com.lagom.solarinverterbot.service;

import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import br.com.lagom.solarinverterbot.model.InverterManufacturer;
import br.com.lagom.solarinverterbot.model.Plant;
import br.com.lagom.solarinverterbot.model.PortalQueueEntry;
import br.com.lagom.solarinverterbot.repository.PlantQueueEntryRepository;
import br.com.lagom.solarinverterbot.repository.PortalQueueEntryRepository;
import br.com.lagom.solarinverterbot.model.PlantQueueEntry;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class PortalQueueService {

    @Autowired
    private PlantQueueEntryRepository plantQueueEntryRepository;

    @Autowired
    private PortalQueueEntryRepository portalQueueEntryRepository;

    @Autowired
    private GrowattScraperService growattScraperService;

    @Scheduled(fixedDelay = 5000)
    public void verifyQueuesToProcess(){
        log.info("Verificando se existem filas pendentes.");

        List<PortalQueueEntry> portalQueueEntryPending = portalQueueEntryRepository.findAllByStatusQueue(StatusQueueEnum.PENDING);

        if(portalQueueEntryPending.isEmpty()){
            log.info("Não há filas pendentes no momento.");
            return;
        }

        for(PortalQueueEntry entry: portalQueueEntryPending){
            processQueueAsync(entry);
        }
    }

    @Async
    @Transactional
    public void processQueueAsync(PortalQueueEntry entry){
        log.info("Iniciando processamento da fila: {} ", entry.getId());
        entry.setStatusQueue(StatusQueueEnum.IN_PROGRESS);
        portalQueueEntryRepository.save(entry);
        List<PlantQueueEntry> plants = Collections.singletonList(plantQueueEntryRepository.findById(entry.getId())
                .orElseThrow(() -> new EntityNotFoundException("fila não encontrada.")));
        for(PlantQueueEntry plant: plants){
            startScraping(plant.getPlant(), entry.getInverterManufacturer());
        }
        entry.setStatusQueue(StatusQueueEnum.COMPLETED);
        portalQueueEntryRepository.save(entry);
    }

    @Async
    public void startScraping(Plant plant, InverterManufacturer manufacturer){
        if(manufacturer.getName().equalsIgnoreCase("GROWATT")){
            growattScraperService.isPortalAvailable(manufacturer.getName());
            growattScraperService.webScrapingService(plant);
        }
    }



}
