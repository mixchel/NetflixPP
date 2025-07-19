package com.seed.databaseseed.entities.relationalModel;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "Investimento")
public class Investment implements Serializable {
    @EmbeddedId
    private SharkProjectPK id = new SharkProjectPK();
    @Column(name = "valor_do_investimento")
    private Double investmentValue;
    @Column(name = "porcentagem_vendida_do_projeto")
    private Double projectPercentageAcquired;

    public Investment() {
    }

    public Investment(Shark shark, Project project, Double investmentValue, Double projectPercentageAcquired) {
        id.setProject(project);
        id.setShark(shark);
        this.investmentValue = investmentValue;
        this.projectPercentageAcquired = projectPercentageAcquired;
    }

    public Shark getShark(){ return id.getShark(); }

    public Project getProject(){ return id.getProject(); }

    public void setProject(Project project){
        id.setProject(project);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Investment that = (Investment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}