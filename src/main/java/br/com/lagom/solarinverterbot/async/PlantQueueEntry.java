package br.com.lagom.solarinverterbot.async;

import br.com.lagom.solarinverterbot.enums.StatusPlantQueueEnum;
import br.com.lagom.solarinverterbot.model.Plant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantQueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "queue_entry_id", nullable = false)
    private QueueEntry queueEntry;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    private ZonedDateTime createdAt = ZonedDateTime.now();

    private ZonedDateTime processedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusPlantQueueEnum statusPlantQueue;

}
