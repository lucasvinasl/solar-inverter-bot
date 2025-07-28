package br.com.lagom.solarinverterbot.controller;

import br.com.lagom.solarinverterbot.dto.ImportCredentialsQueueResponseDTO;
import br.com.lagom.solarinverterbot.dto.request.ImportCredentialsRequestDTO;
import br.com.lagom.solarinverterbot.model.UserAccount;
import br.com.lagom.solarinverterbot.service.ImportCredentialsService;
import br.com.lagom.solarinverterbot.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/credential")
public class ImportCredentialsController {

    @Autowired
    private ImportCredentialsService importCredentialsService;

    @Autowired
    private UserAccountService userAccountService;

    @PostMapping("/update-spreadsheet")
    public ResponseEntity<ImportCredentialsQueueResponseDTO> consumeSpreadsheetClient(@RequestBody ImportCredentialsRequestDTO importDTO){
        try{
            UserAccount user = userAccountService.findById(importDTO.userId());
            String path = importDTO.sheet();
            importCredentialsService.addImportCredentialsQueue(path, user.getCompany());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }

    @GetMapping
    public ResponseEntity<Page<ImportCredentialsQueueResponseDTO>> getImportCredentialsQueue(@RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                                                       @RequestParam(value = "size", required = false, defaultValue = "100") int size,
                                                                                       @RequestParam(value = "companyId", required = true) Long companyId){
        Page<ImportCredentialsQueueResponseDTO> responseDTO = importCredentialsService.getAllImportCredentialsQueue(companyId, page, size);
        return ResponseEntity.ok(responseDTO);
    }

}
