package br.com.lagom.solarinverterbot.scraper;

import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import br.com.lagom.solarinverterbot.model.InverterManufacturer;
import br.com.lagom.solarinverterbot.model.Plant;
import br.com.lagom.solarinverterbot.service.GrowattScraperService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class QueueService {

    @Autowired
    private PlantQueueEntryRepository plantQueueEntryRepository;

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @Autowired
    private GrowattScraperService growattScraperService;

    @Scheduled(fixedDelay = 5000)
    public void verifyQueuesToProcess(){
        log.info("Verificando se existem filas pendentes.");

        List<QueueEntry> queueEntryPending = queueEntryRepository.findAllByStatusQueue(StatusQueueEnum.PENDING);

        if(queueEntryPending.isEmpty()){
            log.info("Não há filas pendentes no momento.");
            return;
        }

        for(QueueEntry entry: queueEntryPending){
            processQueueAsync(entry);
        }
    }

    @Async
    @Transactional
    public void processQueueAsync(QueueEntry entry){
        log.info("Iniciando processamento da fila: {} ", entry.getId());
        entry.setStatusQueue(StatusQueueEnum.IN_PROGRESS);
        queueEntryRepository.save(entry);
        List<PlantQueueEntry> plants = plantQueueEntryRepository.findByQueueEntry(entry);
        for(PlantQueueEntry plant: plants){
            startScraping(plant.getPlant(), entry.getInverterManufacturer());
        }
        entry.setStatusQueue(StatusQueueEnum.COMPLETED);
        queueEntryRepository.save(entry);
    }

    @Async
    public void startScraping(Plant plant, InverterManufacturer manufacturer){
        if(manufacturer.getName().equalsIgnoreCase("GROWATT")){
            growattScraperService.isPortalAvailable(manufacturer.getName());
            growattScraperService.webScrapingService(plant);
        }
    }



}
