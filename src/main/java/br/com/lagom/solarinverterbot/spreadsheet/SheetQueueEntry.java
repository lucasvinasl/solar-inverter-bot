package br.com.lagom.solarinverterbot.spreadsheet;


import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import br.com.lagom.solarinverterbot.model.Plant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SheetQueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plant_id")
    private Plant plant;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusQueueEnum statusSheet;


    public SheetQueueEntry(Plant plant, String filePath, String fileName) {
        this.plant = plant;
        this.filePath = filePath;
        this.fileName = fileName;
        this.createdAt = ZonedDateTime.now();
        this.statusSheet = StatusQueueEnum.PENDING;
    }


}
