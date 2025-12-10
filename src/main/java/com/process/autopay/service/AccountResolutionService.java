package com.process.autopay.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AccountResolutionService {

    private static final Logger logger = LoggerFactory.getLogger(AccountResolutionService.class);

    private final BankAggregatorApiClient bankAggregatorApiClient;

    @Autowired
    public AccountResolutionService(BankAggregatorApiClient bankAggregatorApiClient) {
        this.bankAggregatorApiClient = bankAggregatorApiClient;
    }

    // Reactive implementation: returns a Mono that emits the raw institutions JSON
    public Mono<String> resolveAccount() {
        // Auth header is now added by the WebClient interceptor, so call the client directly
        return bankAggregatorApiClient.getInstitutionByName("USAA")
                .doOnNext(banksJson -> logger.info("Fetched institutions response: {}", banksJson));
    }
}
