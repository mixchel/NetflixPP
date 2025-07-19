package com.seed.databaseseed.entities.relationalModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SharkProjectPK implements Serializable {
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shark_id")
    private Shark shark;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "projeto_id")
    private Project project;

    public Shark getShark() {
        return shark;
    }

    public void setShark(Shark shark) {
        this.shark = shark;
    }

    public Project getProject() { return project; }

    public void setProject(Project project) { this.project = project; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharkProjectPK that = (SharkProjectPK) o;
        return Objects.equals(getShark(), that.getShark()) && Objects.equals(getProject(), that.getProject());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getShark(), getProject());
    }
}