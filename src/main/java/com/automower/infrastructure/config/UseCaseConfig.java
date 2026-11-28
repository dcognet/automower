package com.automower.infrastructure.config;

import com.automower.application.MowLawnUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    // le cas d'usage reste sans annotation Spring : il est déclaré ici, côté infrastructure
    @Bean
    public MowLawnUseCase mowLawnUseCase() {
        return new MowLawnUseCase();
    }
}
