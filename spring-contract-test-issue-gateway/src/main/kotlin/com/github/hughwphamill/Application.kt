package com.github.hughwphamill

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class SpringContractTestIssueApplication

fun main(args: Array<String>) {
    runApplication<SpringContractTestIssueApplication>(*args)
}