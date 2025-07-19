package com.seed.databaseseed.entities.relationalModel;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "Projeto")
public class Project implements Serializable {
    @Id
    //I manually set the IDs
    @Column(name = "projeto_id")
    private Integer id;
    @Column(name = "nome")
    private String name;
    private String website;
    @Column(name = "valor_de_mercado")
    private Double valuation;
    @Column(name = "categoria")
    private String category;
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String description;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "numero_do_episodio")
    private Episode episode;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Entrepreneur> entrepreneurs = new ArrayList<>();
    @OneToMany(mappedBy = "id.project", cascade = CascadeType.ALL)
    private List<Investment> investments = new ArrayList<>();

    public Project(Integer id, String name, String website, Double valuation, String category, String description) {
        this.id = id;
        this.name = name;
        this.website = website;
        this.valuation = valuation;
        this.category = category;
        this.description = description;
    }

    public Project(Integer id, String name, String website, Double valuation, String category, String description, Episode episode) {
        this.id = id;
        this.name = name;
        this.website = website;
        this.valuation = valuation;
        this.category = category;
        this.description = description;
        this.episode = episode;
    }

    public Project() {
    }

    public Project(Integer picht, String projectName, String website, String category, String description) {
        this.id = id;
        this.name = name;
        this.website = website;
        this.valuation = valuation;
        this.category = category;
        this.description = description;

    }

    public void setEpisode(Episode episode) { this.episode = episode; }

    public void setInvestments(List<Investment> investments) { this.investments = investments; }

    public void setEntrepreneurs(List<Entrepreneur> entrepreneurs) {
        this.entrepreneurs = entrepreneurs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
