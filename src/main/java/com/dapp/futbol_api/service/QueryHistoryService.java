package com.dapp.futbol_api.service;

import com.dapp.futbol_api.model.QueryHistory;
import com.dapp.futbol_api.model.QueryType;
import com.dapp.futbol_api.repositories.QueryHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryHistoryService {

    private static final Logger log = LoggerFactory.getLogger(QueryHistoryService.class);
    private final QueryHistoryRepository queryHistoryRepository;

    /**
     * Saves a query record to the history.
     */
    public void saveQuery(Long userId, String playerName, QueryType queryType) {
        QueryHistory historyEntry = QueryHistory.builder()
                .userId(userId)
                .playerName(playerName)
                .queryType(queryType)
                .queryDate(LocalDate.now())
                .build();
        queryHistoryRepository.save(historyEntry);
        log.info("Saved {} query for player '{}' by user {}.", queryType, playerName, userId);
    }

    /**
     * Retrieves query history for a given date and player name.
     */
    public List<QueryHistory> getHistoryByDateAndPlayer(LocalDate date, String playerName) {
        return queryHistoryRepository.findByQueryDateAndPlayerName(date, playerName);
    }
}