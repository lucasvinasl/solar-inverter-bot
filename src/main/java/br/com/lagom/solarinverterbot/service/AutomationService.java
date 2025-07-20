package br.com.lagom.solarinverterbot.service;

import br.com.lagom.solarinverterbot.dto.PlantsToScrapingRequestDTO;
import br.com.lagom.solarinverterbot.dto.PortalPlantCredentialImportDTO;
import br.com.lagom.solarinverterbot.enums.StarterTypeEnum;
import br.com.lagom.solarinverterbot.model.Client;
import br.com.lagom.solarinverterbot.model.Plant;
import br.com.lagom.solarinverterbot.model.PlantCredential;
import br.com.lagom.solarinverterbot.spreadsheet.PortalPlantCredentialImportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class AutomationService {

    @Autowired
    private PortalPlantCredentialImportService portalPlantCredentialImportService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private PlantService plantService;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicInteger lastProcessedClientIndex = new AtomicInteger(-1);


    public List<Plant> plantsToScraping(PlantsToScrapingRequestDTO form){

        if(form.manufacturerId() == null){
            throw new IllegalArgumentException("Fabricante deve ser informado para iniciar a automação.");
        }

        return switch (form.starterType()) {
            case StarterTypeEnum.INITIAL ->
                    plantService.findAllByManufacturerId(form.manufacturerId());

            case StarterTypeEnum.RESUME ->
                    plantService.findAllFromIdAndManufacturerId(form.manufacturerId(), form.plantId());
        };

    }


    // PAREI AQUI, TENHO QUE PENSAR EM UMA MANEIRA DE CONTROLAR O INICIAR, PAUSAR E RETOMAR A FILA.
    /*
        SEMPRE VAI SER UM FABRICANTE POR VEZ.
        INICIAR A FILA: PEGA TODAS AS PLANTAS DE UM MESMO FABRICANTE.
        PAURSAR: EXECUTA QUEM TÁ NA VEZ MAS NÃO VAI PRO PRÓXIMO, SALVA O ÚLTIMO QUE FEZ.
        RESUME: INICIA NO ÚLTIMO PAUSADO +1;

        plantsToScraping -> foi um começo
     */
    public void executeScraping(PlantsToScrapingRequestDTO form) {
        if (!isRunning.compareAndSet(false, true)) {
            log.info("Iniciando processo de Scraping.");
            return;
        }

        try {

            isPaused.set(false);

            List<Plant> plants = plantsToScraping(form);
            if(plants.isEmpty()){
                throw new EntityNotFoundException("Sem clientes cadastrados para esse fabricante.");
            }

//            List<Client> clients;
//            try {
//                clients = clientService.findAll();
//            } catch (EntityNotFoundException e) {
//                log.error("Erro ao buscar clientes: {}", e.getMessage());
//                isRunning.set(false);
//                throw e;
//            }

            int startIndex = Math.max(0, lastProcessedClientIndex.get() + 1); // acho que é melhor trabalhar diretamente com o ID do cliente
            // o processo sempre vai começar do primeiro cliente. caso tenha sido um pause e agora é um resume, basta pegar o último cliente processado.
            // pensando bem eu posso deixar a lista como controle do fluxo.
            // mas é interessante usar o próprio banco só pra dar o resume, porque caso a aplicação caia, eu sempre vou ter o último cliente processado,
            // independente da lista em memória.

            if (startIndex == 0) {
                lastProcessedClientIndex.set(-1);
                log.info("Processando lista completa de clientes.");
            }

            log.info("Processando a partir de: {}", startIndex);

            for (int i = startIndex; i < clients.size(); i++) {
                if (!isRunning.get()) {
                    log.info("Solicitação de Cancalamento verificada.");
                    return;
                }

                if (isPaused.get()) {
                    log.info("Solicitação de Pausa verificada. Scraping pausado no cliente: {}", i - 1);
                    lastProcessedClientIndex.set(i - 1);
                    return;
                }

                Client client = clients.get(i);
                String manufacturerName = client.getInverterManufacturer().getName();

                // Testando só a Growatt
                if (!manufacturerName.equalsIgnoreCase(Manufacturers.GROWATT.name())) {
                    lastProcessedClientIndex.set(i);
                    continue;
                }

                scrapers.stream()
                        .filter(scraper -> scraper.isPortalAvailable(manufacturerName))
                        .findFirst()
                        .ifPresentOrElse(
                                scrapers -> scrapers.webScrapingService(client),
                                () -> log.info("Nenhum Scraper implementado para o Fabricante: {} \n", manufacturerName)
                        );

                if(isRunning.get()){
                    lastProcessedClientIndex.set(i);
                }else{
                    lastProcessedClientIndex.set(-1);
                }

            }

            log.info("Scraping process completed successfully");
        } finally {
            isRunning.set(false);
        }
    }

    public boolean cancelScraping() {
        if (isRunning.get()) {
            isRunning.set(false);
            isPaused.set(false);
            lastProcessedClientIndex.set(-1);
            log.info("Scraping cancelado.");
            return true;
        }
        log.info("Não há Scraping em andamento para cancelar.");
        return false;
    }

    public boolean pauseScraping() {
        if (isRunning.get() && !isPaused.get()) {
            isPaused.set(true);
            log.info("Scraping pausado.");
            return true;
        }
        log.info("Não há Scraping em andamento para pausar.");
        return false;
    }

    public boolean resumeScraping() {
        if (isPaused.get()) {
            isPaused.set(false);
            log.info("Reiniciando Scrapring a partir de: {}", lastProcessedClientIndex.get());
            executeScraping();
            return true;
        }
        log.info("Não há Scraping em andamento para continuar.");
        return false;
    }

    public ScrapingStatusEnum getScrapingStatus() {
        if (isRunning.get()) {
            return ScrapingStatusEnum.RUNNING;
        }else if (isPaused.get()) {
            return ScrapingStatusEnum.PAUSED;
        }else{
            return ScrapingStatusEnum.INACTIVE;
        }
    }


    public PortalPlantCredentialImportDTO updateSpreadsheetClients(){
        File file = new File("C:/Users/usuario/Documents/SpreadsheetAutomation/Client_Credentials_By_Manufacturer.xlsx");
        return portalPlantCredentialImportService.importClientsCredentials(file);
    }
}
