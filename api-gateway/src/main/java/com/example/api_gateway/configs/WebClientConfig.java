package com.example.api_gateway.configs;


// config/WebClientConfig.java

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.*;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class WebClientConfig {

    @Bean("authClient")
    WebClient authClient(@Value("${auth.base-url}") String baseUrl) {
        return base(baseUrl);
    }

    @Bean("usersClient")
    WebClient usersClient(@Value("${users.base-url}") String baseUrl) {
        return base(baseUrl);
    }

    @Bean("devicesClient")
    WebClient devicesClient(@Value("${devices.base-url}") String baseUrl) {
        return base(baseUrl);
    }

    private WebClient base(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5))
                .compress(true);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> {
                    var id = request.headers().getFirst("X-Correlation-Id");
                    if (id == null || id.isBlank()) {
                        request.headers().set("X-Correlation-Id", UUID.randomUUID().toString());
                    }
                    return next.exchange(request);
                })
                .build();
    }
}
