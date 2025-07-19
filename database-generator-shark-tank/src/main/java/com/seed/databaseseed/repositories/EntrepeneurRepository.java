package com.seed.databaseseed.repositories;

import com.seed.databaseseed.entities.relationalModel.Entrepreneur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrepeneurRepository extends JpaRepository<Entrepreneur, Integer> {
}
