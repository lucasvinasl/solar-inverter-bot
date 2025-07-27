package br.com.lagom.solarinverterbot.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Client {

    /*
        Esse cliente vai ser a pessoa física/jurídica, então ele pode ter várias usinas/plantas.
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String email;

    private String identifier;

    private String phone;

//    @ManyToOne
//    @JoinColumn(name = "company_id", nullable = false)
//    private Company company;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plant> plants = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlantCredential> credentials = new ArrayList<>();

}
