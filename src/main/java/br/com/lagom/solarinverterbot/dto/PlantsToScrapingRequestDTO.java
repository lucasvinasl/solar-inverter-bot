package br.com.lagom.solarinverterbot.dto;

import br.com.lagom.solarinverterbot.enums.StarterTypeEnum;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

@Builder
@Jacksonized
public record PlantsToScrapingRequestDTO(
        Long plantId,
        Long manufacturerId,
        StarterTypeEnum starterType
) implements Serializable {
}
