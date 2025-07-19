package com.seed.databaseseed.repositories;

import com.seed.databaseseed.entities.relationalModel.Investment;
import com.seed.databaseseed.entities.relationalModel.SharkProjectPK;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentRepository extends JpaRepository<Investment, SharkProjectPK> {
}
