# spring-contract-test-issue

Spring Cloud Contract ```@AutoConfigureStubRunner``` does not work with Spring Reactive Loadbalancer

## Reproducing the Issue

run ```./mvnw clean install```

The generated contract test will fail with 503 "unable to find instance for example-service"

Change to Ribbon by setting 

```yml
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: true
```

in spring-contract-test-issue-gateway/src/test/resources/application.yml

run ```./mvnw clean install```

The generated contract test will now pass (the route test will fail as it's using the spring loadbalancer).
