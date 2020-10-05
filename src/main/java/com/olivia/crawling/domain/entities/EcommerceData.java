package com.olivia.crawling.domain.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "ECOMMERCEDATA")
@NoArgsConstructor
public class EcommerceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long Id;

    @Column
    @Getter @Setter
    private String produtoNome;

    @Column
    @Getter @Setter
    private String url;

    @Column
    @Getter @Setter
    private String categoria;

    @Column
    @Getter @Setter
    private double peso;

    @Column
    @Getter @Setter
    private String cor;

    @Column
    @Getter @Setter
    private double desconto;

    @Column
    @Getter @Setter
    private float classificacao;

    @Column
    @Getter @Setter
    private float preco;


}
