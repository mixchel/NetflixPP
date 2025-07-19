package com.seed.databaseseed.repositories;

import com.seed.databaseseed.entities.relationalModel.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
