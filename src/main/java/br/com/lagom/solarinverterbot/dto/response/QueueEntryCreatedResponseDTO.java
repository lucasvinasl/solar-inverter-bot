package br.com.lagom.solarinverterbot.dto.response;

public record QueueEntryCreatedResponseDTO(
        String manufacturerName,
        int plantsToScraping
) {
}
