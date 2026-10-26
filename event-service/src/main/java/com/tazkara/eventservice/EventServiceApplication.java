package com.tazkara.eventservice;

import com.tazkara.eventservice.config.DatabaseCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EventServiceApplication.class);
        app.addListeners(new DatabaseCreator());
        app.run(args);
    }
}