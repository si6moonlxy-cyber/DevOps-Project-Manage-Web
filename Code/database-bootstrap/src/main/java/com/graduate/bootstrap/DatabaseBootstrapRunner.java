package com.graduate.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseBootstrapRunner implements CommandLineRunner {

    private final DatabaseBootstrapService bootstrapService;

    public DatabaseBootstrapRunner(DatabaseBootstrapService bootstrapService) {
        this.bootstrapService = bootstrapService;
    }

    @Override
    public void run(String... args) throws Exception {
        bootstrapService.bootstrap();
    }
}
