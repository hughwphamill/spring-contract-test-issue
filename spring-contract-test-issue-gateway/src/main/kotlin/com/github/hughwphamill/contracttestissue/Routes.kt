package com.github.hughwphamill.contracttestissue

import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunctions.route

@Configuration
class TestRoutes {

    @Bean
    fun routes(builder: RouteLocatorBuilder) = builder.routes {
        route("testRoute") {
            uri("lb://external-service")
            path("/gwpath")
            filters {
                setPath("/test/resource")
            }
        }
    }
}