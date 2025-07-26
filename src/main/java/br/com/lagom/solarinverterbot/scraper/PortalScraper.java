package br.com.lagom.solarinverterbot.scraper;

import br.com.lagom.solarinverterbot.model.Plant;

public interface PortalScraper {
    boolean isPortalAvailable(String manufacturerName);
    void webScrapingService(Plant plant);
}
