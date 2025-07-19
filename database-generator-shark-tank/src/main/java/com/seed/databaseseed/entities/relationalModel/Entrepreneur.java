package com.seed.databaseseed.entities.relationalModel;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "Empreendedor")
public class Entrepreneur implements Serializable {
    @Id
    //I manually set the IDs
    @Column(name = "empreendedor_id")
    private Integer id;
    @Column(name = "nome")
    private String name;
    @Column(name = "genero")
    private String gender;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "projeto_id")
    private Project project;

    public Entrepreneur(Integer id, String name, String gender, Project project) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.project = project;
    }

    public Entrepreneur() {
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entrepreneur that = (Entrepreneur) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
