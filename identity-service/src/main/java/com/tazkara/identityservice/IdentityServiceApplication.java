package com.tazkara.identityservice;

import com.tazkara.identityservice.config.DatabaseCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(IdentityServiceApplication.class);
        app.addListeners(new DatabaseCreator());
        app.run(args);
    }
}