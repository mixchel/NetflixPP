package com.seed.databaseseed.repositories;

import com.seed.databaseseed.entities.relationalModel.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<Episode, Integer> {
}
