package br.com.lagom.solarinverterbot.dto;

public record QueueEntryCreatedResponseDTO(
        String manufacturerName,
        int plantsToScraping
) {
}
