package br.com.lagom.solarinverterbot.dto;

import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;

public record ImportCredentialsQueueResponseDTO(Long id, int saved, int invalid, StatusQueueEnum status) {
}
