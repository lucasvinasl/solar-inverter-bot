package br.com.lagom.solarinverterbot.model;

import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportCredentialsQueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    private ZonedDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusQueueEnum statusCredentials;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    private int saved;

    private int invalid;

    public ImportCredentialsQueueEntry(String filePath) {
        this.filePath = filePath;
        this.createdAt = ZonedDateTime.now();
        this.statusCredentials = StatusQueueEnum.PENDING;
    }

}
