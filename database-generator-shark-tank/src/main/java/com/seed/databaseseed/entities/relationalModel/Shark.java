package com.seed.databaseseed.entities.relationalModel;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

@Entity
public class Shark implements Serializable {
    @Id
    //I manually set the IDs
    @Column(name = "shark_id")
    private Integer id;
    @Column(name = "nome")
    private String name;
    @ManyToMany(mappedBy = "sharks", cascade = CascadeType.ALL)
    private Set<Episode> episodes = new HashSet<>();
    @OneToMany(mappedBy = "id.shark")//, cascade = CascadeType.ALL)
    private List<Investment> investments = new ArrayList<>();

    public Shark(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Shark() {
    }

    public void addEpisode(Episode episode) {
        episodes.add(episode);
    }

    public void addInvestment(Investment investment) {
        investments.add(investment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shark shark = (Shark) o;
        return Objects.equals(id, shark.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
