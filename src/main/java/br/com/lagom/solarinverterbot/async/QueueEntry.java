package br.com.lagom.solarinverterbot.async;

import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import br.com.lagom.solarinverterbot.model.InverterManufacturer;
import br.com.lagom.solarinverterbot.model.UserAccount;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount creator;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "inverter_manufacturer_id")
    private InverterManufacturer inverterManufacturer;

    @NotNull
    private ZonedDateTime createdAt = ZonedDateTime.now();

    private ZonedDateTime processedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusQueueEnum statusQueue;

}
