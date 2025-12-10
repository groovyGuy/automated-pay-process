package com.process.autopay.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.process.autopay.service.AccountResolutionService;

import reactor.core.publisher.Mono;

@RestController
public class FinanceResolutionController {

    private final AccountResolutionService accountResolutionService;

    @Autowired
    public FinanceResolutionController(AccountResolutionService accountResolutionService) {
        this.accountResolutionService = accountResolutionService;
    }

    @GetMapping("/")
    public Mono<String> evaluateAccount() {
        return accountResolutionService.resolveAccount();
    }
}
