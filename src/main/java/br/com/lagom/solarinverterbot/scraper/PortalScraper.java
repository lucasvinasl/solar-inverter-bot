package br.com.lagom.solarinverterbot.scraper;

import br.com.lagom.solarinverterbot.model.Client;

public interface PortalScraper {
    boolean isPortalAvailable(String manufacturerName);
    void webScrapingService(Client client);
}
