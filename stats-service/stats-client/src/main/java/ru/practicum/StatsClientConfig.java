package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;

@Configuration
public class StatsClientConfig {

    @Bean
    public RestClient statsRestClient(@Value("${stats-server.url}") String serverUrl) {
        return RestClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    @Bean
    public String appName(@Value("${application.name}") String appName) {
        return appName;
    }

    @Bean
    public DateTimeFormatter statsDateTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
}

