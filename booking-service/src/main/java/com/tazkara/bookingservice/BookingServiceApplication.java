package com.tazkara.bookingservice;

import com.tazkara.bookingservice.config.DatabaseCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
@EnableScheduling
public class BookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BookingServiceApplication.class);
        app.addListeners(new DatabaseCreator());
        app.run(args);
    }
}