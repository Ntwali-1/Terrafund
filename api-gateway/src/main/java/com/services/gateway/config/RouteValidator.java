package com.services.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

        public static final List<String> openApiEndpoints = List.of(
                        "/api/auth/signup",
                        "/api/auth/login",
                        "/api/auth/verify-email",
                        "/api/auth/resend-verification",
                        "/eureka",
                        "/swagger-ui",
                        "/v3/api-docs",
                        "/swagger-ui.html",
                        "/webjars/",
                        "/swagger-resources",
                        "/swagger-config",
                        "/api-docs",
                        "/user-service/v3/api-docs",
                        "/investment-service/v3/api-docs",
                        "/land-service/v3/api-docs",
                        "/actuator");

        public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints
                        .stream()
                        .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
