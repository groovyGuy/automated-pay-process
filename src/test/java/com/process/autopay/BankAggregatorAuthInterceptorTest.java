package com.process.autopay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.process.autopay.service.BankAggregatorAuthProvider;

import reactor.core.publisher.Mono;

public class BankAggregatorAuthInterceptorTest {

    @Test
    public void interceptorAddsAuthorizationHeader() throws Exception {
        // Arrange: create auth provider with known values
        String userId = "test-user";
        // base64 of 'secret' -> c2VjcmV0
        String accessKey = "c2VjcmV0";
        // BankAggregatorAuthProvider constructor now expects (userId, accessKey)
        BankAggregatorAuthProvider authProvider = new BankAggregatorAuthProvider(userId, accessKey);

        // Mock exchange function to capture request
        ExchangeFunction mockExchange = mock(ExchangeFunction.class);
        when(mockExchange.exchange(any(ClientRequest.class))).thenAnswer(invocation -> {
            ClientRequest req = invocation.getArgument(0);
            // Return a simple OK response
            ClientResponse mockResponse = ClientResponse.create(org.springframework.http.HttpStatus.OK).build();
            return Mono.just(mockResponse);
        });

        ExchangeFilterFunction authFilter = ExchangeFilterFunction.ofRequestProcessor(request -> {
            try {
                String auth = authProvider.generateAuthCode(request.url().toString(), request.method().name());
                ClientRequest newReq = ClientRequest.from(request).headers(h -> h.set("Authorization", auth)).build();
                return Mono.just(newReq);
            } catch (Exception e) {
                return Mono.error(e);
            }
        });

        WebClient client = WebClient.builder().filter(authFilter).exchangeFunction(mockExchange).build();

        // Act: perform a GET exchange using exchangeToMono (exchange() was removed)
        client.get()
              .uri(URI.create("https://api.test/Institution/GetInstitutionByName"))
              .exchangeToMono(response -> Mono.just(response))
              .block();

        // Assert: capture the request and verify Authorization header present
        verify(mockExchange, times(1)).exchange(any(ClientRequest.class));
        ArgumentCaptor<ClientRequest> captor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(mockExchange).exchange(captor.capture());
        ClientRequest captured = captor.getValue();
        assertNotNull(captured.headers().getFirst("Authorization"));
        String authValue = captured.headers().getFirst("Authorization");
        assertTrue(authValue.startsWith("FIApiAUTH:"));
    }
}
