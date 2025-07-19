package com.seed.databaseseed.entities.relationalModel;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Episodio")
public class Episode implements Serializable {
    @Id
    //I manually set the IDs
    @Column(name = "numero_do_episodio")
    private Integer number;
    @Column(name = "temporada")
    private Integer season;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "episodio_shark",
               joinColumns = @JoinColumn(name = "numero_do_episodio"),
               inverseJoinColumns = @JoinColumn(name = "shark_id"))
    private Set<Shark> sharks = new HashSet<>();
    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private Set<Project> projects = new HashSet<>();

    public Episode(Integer number, Integer season) {
        this.number = number;
        this.season = season;
    }

    public Episode() {}

    public Set<Shark> getSharks() {
        return sharks;
    }

    public void setSharks(Set<Shark> sharks) {
        this.sharks = sharks;
    }

    public void addProject(Project project){
        projects.add(project);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Episode episode = (Episode) o;
        return Objects.equals(number, episode.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
