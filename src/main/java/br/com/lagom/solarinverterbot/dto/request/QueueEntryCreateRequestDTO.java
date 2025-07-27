package br.com.lagom.solarinverterbot.dto.request;

import br.com.lagom.solarinverterbot.enums.StarterTypeEnum;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

@Builder
@Jacksonized
public record QueueEntryCreateRequestDTO(
        Long manufacturerId,
        StarterTypeEnum starterType,
        Long creatorId
) implements Serializable {
}
