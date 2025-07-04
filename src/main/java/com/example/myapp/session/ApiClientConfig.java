package com.example.myapp.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiClientConfig {

  @Bean
  public WebClient apiClient(AuthSession session) {

    ExchangeFilterFunction authFilter = (request, next) -> {
      if (session.isAuthenticated()) {
        ClientRequest req = ClientRequest.from(request)
            .header(HttpHeaders.AUTHORIZATION, session.getAuthHeader())
            .build();
        return next.exchange(req);
      }
      return next.exchange(request);
    };

    return WebClient.builder()
        .baseUrl("http://localhost:8080/api")   // ajuste si besoin
        .filter(authFilter)
        .build();
  }
}