package com.github.hughwphamill

import io.restassured.RestAssured
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureStubRunner(ids = ["com.github.hughwphamill:spring-contract-test-issue-contracts"])
abstract class ContractBase {

    @LocalServerPort
    private var port: Int = 8086

    @Before
    fun setUp() {
        RestAssured.baseURI = "http://localhost:$port"
    }
}