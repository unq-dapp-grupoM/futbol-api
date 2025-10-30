package com.dapp.futbol_api.repositories;

import com.dapp.futbol_api.model.QueryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Long> {

    List<QueryHistory> findByQueryDateAndPlayerName(LocalDate queryDate, String playerName);
}