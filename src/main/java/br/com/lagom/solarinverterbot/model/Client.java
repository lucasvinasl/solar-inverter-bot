package br.com.lagom.solarinverterbot.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Plant> plantList;

}
