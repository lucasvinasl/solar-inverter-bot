package br.com.lagom.solarinverterbot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String code;

    private BigDecimal power;

    private BigDecimal monthlyExpectedGeneration;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inverter> inverterList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "credential_id")
    private PlantCredential credential;

    @ManyToOne
    @JoinColumn(name = "inverter_manufacturer_id")
    private InverterManufacturer inverterManufacturer;

}
