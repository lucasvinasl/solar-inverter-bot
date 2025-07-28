package br.com.lagom.solarinverterbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ImportCredentialsQueueScheduled {

    @Autowired
    private ImportCredentialsService importCredentialsService;

    @Scheduled(fixedDelay = 5000)
    public void executeImportCredentialsQueueAsync(){
        importCredentialsService.importSheetCredentials();
    }

}
