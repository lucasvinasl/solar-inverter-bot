package br.com.lagom.solarinverterbot.service;

import br.com.lagom.solarinverterbot.scraper.PlantQueueEntry;
import br.com.lagom.solarinverterbot.scraper.PlantQueueEntryRepository;
import br.com.lagom.solarinverterbot.scraper.QueueEntry;
import br.com.lagom.solarinverterbot.scraper.QueueEntryRepository;
import br.com.lagom.solarinverterbot.dto.request.QueueEntryCreateRequestDTO;
import br.com.lagom.solarinverterbot.dto.PortalPlantCredentialImportDTO;
import br.com.lagom.solarinverterbot.dto.response.QueueEntryCreatedResponseDTO;
import br.com.lagom.solarinverterbot.enums.StarterTypeEnum;
import br.com.lagom.solarinverterbot.enums.StatusPlantQueueEnum;
import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import br.com.lagom.solarinverterbot.model.*;
import br.com.lagom.solarinverterbot.spreadsheet.PortalPlantCredentialImportService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class AutomationService {

    @Autowired
    private PortalPlantCredentialImportService portalPlantCredentialImportService;

    @Autowired
    private PlantService plantService;

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private InverterManufacturerService inverterManufacturerService;

    @Autowired
    private PlantQueueEntryRepository plantQueueEntryRepository;


    public List<Plant> plantsToScraping(QueueEntryCreateRequestDTO form){

        if(form.manufacturerId() == null){
            throw new IllegalArgumentException("Fabricante deve ser informado para iniciar a automação.");
        }

        return switch (form.starterType()) {
            case StarterTypeEnum.INITIAL ->
                    plantService.findAllByManufacturerId(form.manufacturerId());
        };

    }

    /*
        Por enquanto vai ter só o iniciar e cancelar a fila.
     */
    @Transactional
    public QueueEntryCreatedResponseDTO createQueueEntryScraping(QueueEntryCreateRequestDTO form) {
        List<Plant> toScraping = plantsToScraping(form);
        checkIsValidCreateQueue(form.creatorId(), form.manufacturerId());
        try{
            QueueEntry entry = createQueueEntry(form, toScraping);
            return new QueueEntryCreatedResponseDTO(entry.getInverterManufacturer().getName(), toScraping.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkIsValidCreateQueue(Long creatorId, Long manufacturerId){
        UserAccount user = userAccountService.findById(creatorId);
        InverterManufacturer manufacturer = inverterManufacturerService.findById(manufacturerId);
        /*
            no momento que o usuário solicitar o scraping do fabricante X por exemplo, vai criar uma fila.
            enquanto essa fila estiver em processamento nenhuma outra deverá ser iniciada pelo mesmo usuário.

         */
        boolean isRunningByUser = queueEntryRepository.isRunningQueueByUser(user.getId());
        if(isRunningByUser){
            throw new RuntimeException("Já existe uma fila para esse usuário em processamento.");
        }
        /*
            Preciso verificar também se, para a mesma company, não vão pedir a solicitação de scraping para
            o mesmo fabricante ao mesmo tempo.
         */
        Company company = userAccountService.findById(user.getId()).getCompany();
        boolean isOpenQueueByCompanyAndManufacturer = queueEntryRepository.
                isOpenByCompanyIdAndManufacturerId(company.getId(), manufacturer.getId());
        if(isOpenQueueByCompanyAndManufacturer){
            throw new RuntimeException("Já existe uma fila para esse fabricante em processamento.");
        }
    }

    private QueueEntry createQueueEntry(QueueEntryCreateRequestDTO form, List<Plant> plants){

        try{
            QueueEntry queueEntry = new QueueEntry();
            queueEntry.setCreator(userAccountService.findById(form.creatorId()));
            queueEntry.setInverterManufacturer(inverterManufacturerService.findById(form.manufacturerId()));
            queueEntry.setStatusQueue(StatusQueueEnum.PENDING);
            queueEntry = queueEntryRepository.save(queueEntry);

            for(Plant plant: plants){
                PlantQueueEntry plantQueueEntry = new PlantQueueEntry();
                plantQueueEntry.setQueueEntry(queueEntry);
                plantQueueEntry.setPlant(plant);
                plantQueueEntry.setStatusPlantQueue(StatusPlantQueueEnum.PENDING);
                plantQueueEntryRepository.save(plantQueueEntry);
            }

            return queueEntry;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public PortalPlantCredentialImportDTO updateSpreadsheetClients(Company company){
        File file = new File("C:/Users/usuario/Documents/SpreadsheetAutomation/Client_Credentials_By_Manufacturer.xlsx");
        return portalPlantCredentialImportService.importClientsCredentials(file, company);
    }
}
