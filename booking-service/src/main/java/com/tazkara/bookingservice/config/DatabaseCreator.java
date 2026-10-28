package com.tazkara.bookingservice.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseCreator implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();
        String url = env.getRequiredProperty("spring.datasource.url");
        String user = env.getRequiredProperty("spring.datasource.username");
        String password = env.getRequiredProperty("spring.datasource.password");

        int lastSlash = url.lastIndexOf('/');
        String serverUrl = url.substring(0, lastSlash + 1);
        String databaseName = url.substring(lastSlash + 1);

        if (!databaseName.matches("[A-Za-z0-9_]+")) {
            throw new IllegalStateException("Unsafe database name: " + databaseName);
        }

        try (Connection connection = DriverManager.getConnection(serverUrl + "postgres", user, password);
             Statement statement = connection.createStatement()) {

            ResultSet result = statement.executeQuery(
                    "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'");
            if (!result.next()) {
                statement.executeUpdate("CREATE DATABASE " + databaseName);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not create database " + databaseName, e);
        }
    }
}