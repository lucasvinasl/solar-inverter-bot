package br.com.lagom.solarinverterbot.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Inverter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serialNumber;

    private BigDecimal power;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @ManyToOne
    @JoinColumn(name = "inverter_manufacturer_id")
    private InverterManufacturer inverterManufacturer;

    @OneToMany(mappedBy = "inverter", cascade = CascadeType.ALL)
    private List<MonthlyData> monthlyData;

}
