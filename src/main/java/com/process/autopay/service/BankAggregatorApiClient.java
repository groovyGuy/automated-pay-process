package com.process.autopay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class BankAggregatorApiClient {
    private final String baseUrl;
    private final String endpoint;
    private final WebClient webClient;

    @Autowired
    public BankAggregatorApiClient(@Value("${bank.aggregator.url:}") String baseUrl,
                                  @Value("${bank.aggregator.endpoint:}") String endpoint,
                                  @Qualifier("bankAggregatorWebClient") WebClient webClient) {
        this.baseUrl = baseUrl;
        this.webClient = webClient;
        this.endpoint = endpoint;
    }

    public Mono<String> getInstitutionByName(String institutionName) {
        String payload = String.format("{\"InstitutionName\": \"%s\"}", institutionName);
        return post(endpoint, payload);
    }


    private Mono<String> post(String urlPath, String payloadJson) {
        String path = (urlPath != null && urlPath.startsWith("/")) ? urlPath : "/" + (urlPath == null ? "" : urlPath);
        String fullUrl = (this.baseUrl != null ? this.baseUrl : "") + path;

        return webClient.post()
                .uri(fullUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payloadJson == null ? "" : payloadJson)
                .retrieve()
                .bodyToMono(String.class);
    }
}
