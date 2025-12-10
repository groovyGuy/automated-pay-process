package com.process.autopay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import com.process.autopay.service.BankAggregatorAuthProvider;

import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        // Expose a WebClient.Builder bean so other configs/controllers can inject it
        return WebClient.builder();
    }

    @Bean(name = "bankAggregatorWebClient")
    public WebClient bankAggregatorWebClient(WebClient.Builder builder, BankAggregatorAuthProvider authProvider) {
        ExchangeFilterFunction authFilter = ExchangeFilterFunction.ofRequestProcessor(request -> addAuthHeader(request, authProvider));
        return builder
                .filter(authFilter)
                .build();
    }

    private static Mono<ClientRequest> addAuthHeader(ClientRequest request, BankAggregatorAuthProvider authProvider) {
        String url = request.url().toString();
        String method = request.method().name();
        try {
            String auth = authProvider.generateAuthCode(url, method);
            ClientRequest newRequest = ClientRequest.from(request)
                    .headers(headers -> headers.set("Authorization", auth))
                    .build();
            return Mono.just(newRequest);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
