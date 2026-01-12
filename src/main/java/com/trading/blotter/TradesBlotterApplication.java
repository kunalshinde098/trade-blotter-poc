package com.trading.blotter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

@SpringBootApplication
@EnableReactiveElasticsearchRepositories
public class TradesBlotterApplication {
	public static void main(String[] args) {
		SpringApplication.run(TradesBlotterApplication.class, args);
	}
}