package br.com.lagom.solarinverterbot.spreadsheet;

import br.com.lagom.solarinverterbot.dto.SheetInverterDataConsumerDTO;
import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import br.com.lagom.solarinverterbot.model.Inverter;
import br.com.lagom.solarinverterbot.model.MonthlyData;
import br.com.lagom.solarinverterbot.repository.InverterRepository;
import br.com.lagom.solarinverterbot.repository.MonthlyDataRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@EnableScheduling
@Service
public class SheetQueueEntryService {

    @Autowired
    private SheetQueueEntryRepository sheetQueueEntryRepository;

    @Autowired
    private GrowattSheetExtractDataService growattSheetExtractDataService;

    @Autowired
    private InverterRepository inverterRepository;

    @Autowired
    private MonthlyDataRepository monthlyDataRepository;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void scheduleSpreadsheetProcessing(){
        log.info("Verificando se há planilhas pendentes");

        List<SheetQueueEntry> sheetQueueEntries = sheetQueueEntryRepository.findTop10ByStatusSheetOrderByCreatedAtAsc(StatusQueueEnum.PENDING);

        if(sheetQueueEntries.isEmpty()){
            log.info("Nenhuma planilha para processar.");
            return;
        }
        log.info("Novas {} Planilhas pendentes para processar.", sheetQueueEntries.size());

        for(SheetQueueEntry entry: sheetQueueEntries){
            try{
                Optional<SheetQueueEntry> optEntry = sheetQueueEntryRepository.findById(entry.getId());
                if(optEntry.isPresent() && optEntry.get().getStatusSheet().equals(StatusQueueEnum.PENDING)){
                    SheetQueueEntry currentEntry = optEntry.get();
                    currentEntry.setStatusSheet(StatusQueueEnum.IN_PROGRESS);
                    sheetQueueEntryRepository.save(currentEntry);
                    processSheetAsync(currentEntry.getId());
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Async("sheetTaskExecutor")
    @Transactional
    public void processSheetAsync(Long entryId){
       SheetQueueEntry entry = null;
        try{
            Optional<SheetQueueEntry> optEntry = sheetQueueEntryRepository.findById(entryId);
            if (optEntry.isEmpty()) {
                log.warn("Entrada da fila com ID {} não encontrada ou já removida. Pode ter sido processada por outra thread.", entryId);
                return;
            }

            entry = optEntry.get();

            if (entry.getStatusSheet() != StatusQueueEnum.IN_PROGRESS) {
                log.warn("Planilha (ID: {}) não está no status IN_PROGRESS como esperado. Status atual: {}. Ignorando processamento .", entryId, entry.getStatusSheet());
                return;
            }

            log.info("Iniciando extração de dados da planilha");
            File excelfile = new File(entry.getFilePath());
            if(excelfile.exists()){
                if(entry.getPlant().getInverterManufacturer().getName().equalsIgnoreCase("GROWATT")){
                    log.info("Planilha Growatt");
                    List<SheetInverterDataConsumerDTO> growattInverterData = growattSheetExtractDataService.extractMonthlyDataFromSheet(excelfile);
                    if(growattInverterData != null && !growattInverterData.isEmpty()){
                        log.info("Salvando os registros da planilha no banco para {} inversores.", growattInverterData.size());
                        populateEnergyData(growattInverterData, entry);
                    } else {
                        log.warn("Nenhum dado de energia extraído da planilha para o cliente '{}'.", entry.getPlant().getClient().getName());
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void populateEnergyData(List<SheetInverterDataConsumerDTO> allInverters, SheetQueueEntry entry) {
        for (SheetInverterDataConsumerDTO inverterData : allInverters) {
            Long plantId = entry.getPlant().getId();
            String serial = inverterData.getSerialNumber();
            Integer year = inverterData.getYear();

            // Busca (ou cria) o inversor
            Inverter inverter = inverterRepository
                    .findBySerialNumberAndPlantId(serial, plantId)
                    .orElseGet(() -> {
                        Inverter newInverter = new Inverter();
                        newInverter.setSerialNumber(serial);
                        newInverter.setPlant(entry.getPlant());
                        newInverter.setInverterManufacturer(entry.getPlant().getInverterManufacturer());
                        return inverterRepository.save(newInverter);
                    });

            // Busca (ou cria) o registro de geração
            Optional<MonthlyData> existing = monthlyDataRepository
                    .findByYearAndInverterId(year, plantId);

            MonthlyData data = existing.orElseGet(MonthlyData::new);

            data.setYear(year);
            data.setInverter(inverter);

            Map<Integer, Double> monthly = inverterData.getMonthlyGeneration();
            data.setJanuary(monthly.getOrDefault(1, 0.0));
            data.setFebruary(monthly.getOrDefault(2, 0.0));
            data.setMarch(monthly.getOrDefault(3, 0.0));
            data.setApril(monthly.getOrDefault(4, 0.0));
            data.setMay(monthly.getOrDefault(5, 0.0));
            data.setJune(monthly.getOrDefault(6, 0.0));
            data.setJuly(monthly.getOrDefault(7, 0.0));
            data.setAugust(monthly.getOrDefault(8, 0.0));
            data.setSeptember(monthly.getOrDefault(9, 0.0));
            data.setOctober(monthly.getOrDefault(10, 0.0));
            data.setNovember(monthly.getOrDefault(11, 0.0));
            data.setDecember(monthly.getOrDefault(12, 0.0));

            monthlyDataRepository.save(data);

            log.info("{} {} salvo para planta {} (inversor {}).",
                    existing.isPresent() ? "Atualização" : "Criação",
                    year,
                    entry.getPlant().getName(),
                    serial
            );
        }
    }



}
