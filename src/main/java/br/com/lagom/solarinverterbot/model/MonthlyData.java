package br.com.lagom.solarinverterbot.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class MonthlyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Double totalGeneration;

    @Column(name = "january", nullable = false)
    private Double january;

    @Column(name = "february", nullable = false)
    private Double february;

    @Column(name = "march", nullable = false)
    private Double march;

    @Column(name = "april", nullable = false)
    private Double april;

    @Column(name = "may", nullable = false)
    private Double may;

    @Column(name = "june", nullable = false)
    private Double june;

    @Column(name = "july", nullable = false)
    private Double july;

    @Column(name = "august", nullable = false)
    private Double august;

    @Column(name = "september", nullable = false)
    private Double september;

    @Column(name = "october", nullable = false)
    private Double october;

    @Column(name = "november", nullable = false)
    private Double november;

    @Column(name = "december", nullable = false)
    private Double december;

    @ManyToOne
    @JoinColumn(name = "inverter_id", nullable = false)
    private Inverter inverter;

}
