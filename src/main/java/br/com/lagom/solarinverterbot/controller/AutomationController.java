package br.com.lagom.solarinverterbot.controller;

import br.com.lagom.solarinverterbot.dto.PortalPlantCredentialImportDTO;
import br.com.lagom.solarinverterbot.dto.QueueEntryCreateRequestDTO;
import br.com.lagom.solarinverterbot.dto.QueueEntryCreatedResponseDTO;
import br.com.lagom.solarinverterbot.service.AutomationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/automation")
public class AutomationController {

    @Autowired
    private AutomationService automationService;

    @PostMapping("/update-spreadsheet")
    public ResponseEntity<PortalPlantCredentialImportDTO> consumeSpreadsheetClient(){
        return ResponseEntity.ok().body(automationService.updateSpreadsheetClients());
    }

    @PostMapping("/initializer")
    public ResponseEntity<QueueEntryCreatedResponseDTO> createScraper(@RequestBody QueueEntryCreateRequestDTO createForm){
        return ResponseEntity.ok().body(automationService.createQueueEntryScraping(createForm));
    }

}
