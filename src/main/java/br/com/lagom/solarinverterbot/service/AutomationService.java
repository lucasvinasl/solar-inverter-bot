package br.com.lagom.solarinverterbot.service;

import br.com.lagom.solarinverterbot.dto.PortalPlantCredentialImportDTO;
import br.com.lagom.solarinverterbot.spreadsheet.PortalPlantCredentialImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class AutomationService {

    @Autowired
    private PortalPlantCredentialImportService portalPlantCredentialImportService;

    public PortalPlantCredentialImportDTO updateSpreadsheetClients(){
        File file = new File("C:/Users/usuario/Documents/SpreadsheetAutomation/Client_Credentials_By_Manufacturer.xlsx");
        return portalPlantCredentialImportService.importClientsCredentials(file);
    }
}
