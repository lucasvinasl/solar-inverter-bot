package br.com.lagom.solarinverterbot.controller;

import br.com.lagom.solarinverterbot.dto.request.QueueEntryCreateRequestDTO;
import br.com.lagom.solarinverterbot.dto.response.QueueEntryCreatedResponseDTO;
import br.com.lagom.solarinverterbot.service.AutomationService;
import br.com.lagom.solarinverterbot.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/automation")
public class AutomationController {

    @Autowired
    private AutomationService automationService;


    @PostMapping("/initializer")
    public ResponseEntity<QueueEntryCreatedResponseDTO> createScraper(@RequestBody QueueEntryCreateRequestDTO createForm){
        try{
            return ResponseEntity.ok().body(automationService.createQueueEntryScraping(createForm));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
