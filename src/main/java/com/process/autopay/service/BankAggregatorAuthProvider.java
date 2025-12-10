package com.process.autopay.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BankAggregatorAuthProvider {

    private final String userId;
    private final String accessKey;

    public BankAggregatorAuthProvider(@Value("${bank.aggregator.userid:}") String userId,
                                      @Value("${bank.aggregator.secret:}") String accessKey) {
        this.userId = userId;
        this.accessKey = accessKey;
    }

    /**
     * Generate auth code for a specific URL and HTTP method.
     */
    public String generateAuthCode(String url, String httpMethod) throws Exception {
        String method = (httpMethod == null || httpMethod.isBlank()) ? "GET" : httpMethod.toUpperCase();

        if (this.userId == null || this.userId.isBlank()) {
            throw new IllegalStateException("bank.aggregator.userid is not configured");
        }
        if (this.accessKey == null || this.accessKey.isBlank()) {
            throw new IllegalStateException("bank.aggregator.secret is not configured");
        }

        String authPath = "/";
        if (url != null && !url.isBlank()) {
            int idx = url.lastIndexOf('/');
            authPath = (idx >= 0) ? url.substring(idx).toLowerCase() : "/";
        }

        String plainKey = method + "\n" + authPath;

        byte[] keyBytes = Base64.getDecoder().decode(this.accessKey);
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
        byte[] signature = hmac.doFinal(plainKey.getBytes(StandardCharsets.US_ASCII));

        String sigB64 = Base64.getEncoder().encodeToString(signature);
        return String.format("FIApiAUTH:%s:%s:%s", this.userId, sigB64, authPath);
    }
}
