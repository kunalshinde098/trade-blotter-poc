package com.trading.blotter.repository;

import com.trading.blotter.model.TradeDocument;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends ReactiveElasticsearchRepository<TradeDocument, String> {
    // Custom queries can be added here if needed
}