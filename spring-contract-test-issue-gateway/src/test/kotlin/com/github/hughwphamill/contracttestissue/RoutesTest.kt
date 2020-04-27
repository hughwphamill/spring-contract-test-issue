package com.github.hughwphamill.contracttestissue

import com.github.jenspiegsa.wiremockextension.Managed
import com.github.jenspiegsa.wiremockextension.WireMockExtension
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.stubbing.Answer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.client.DefaultServiceInstance
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Flux
import java.time.Duration

import com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with as withWiremock

@ExtendWith(WireMockExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoutesTest {

    @LocalServerPort
    private var port = 9999

    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var mockLoadBalancerClientFactory: LoadBalancerClientFactory

    companion object {
        @Managed
        val testService = withWiremock(wireMockConfig().dynamicPort())!!

        val serviceIdToServerMap = mapOf(
                "external-service" to testService
        )
    }

    @BeforeEach
    fun setUp() {
        println("Running on port $port")
        initWireMock(testService)
        initMockLoadBalancerClientFactory()
        webClient = createWebClient()
    }

    /**
     * Create a new Web Client for testing
     */
    fun createWebClient() = WebTestClient.bindToServer()
            .responseTimeout(Duration.ofSeconds(10))
            .baseUrl("http://localhost:$port")
            .build()

    private fun initWireMock(service: WireMockServer) {
        service.stubFor(WireMock.get(WireMock.urlEqualTo("/test/resource"))
                .willReturn(WireMock.okForContentType("text/html", "Hello from WireMock")))
    }

    /**
     * Stub the Spring Load Balancer Client Factory to return our own hardcoded Load Balancer
     */
    private fun initMockLoadBalancerClientFactory() {
        whenever(mockLoadBalancerClientFactory.getInstance<ServiceInstance>(ArgumentMatchers.anyString(), eq(ReactorLoadBalancer::class.java), eq(ServiceInstance::class.java)))
                .thenAnswer(withTestRoundRobinLoadBalancer())
    }

    /**
     * An Answer which creates a LoadBalancer which knows about the correct WireMock instance
     */
    private fun withTestRoundRobinLoadBalancer() = Answer {
        val serviceId = it.arguments[0] as String
        RoundRobinLoadBalancer(TestServiceInstanceListSupplierProvider(serviceId), serviceId)
    }

    @Test
    fun `route is open`() {
        // When
        val actualResponse = webClient
                .get().uri("/gwpath")
                .exchange().returnResult<String>().responseBody.blockFirst() ?: ""

        // Then
        Assertions.assertThat(actualResponse).isEqualTo("Hello from WireMock")
    }

    @Test
    fun `any other route is closed`() {
        // Given
        testService.stubFor(WireMock.get(WireMock.urlEqualTo("/other"))
                .willReturn(WireMock.aResponse().withBody("Other Response")))

        // When
        val webClientInvocation = webClient
                .get().uri("/rtp/other")
                .exchange()

        // Then
        webClientInvocation.expectStatus().isNotFound
    }
}

/**
 * Instance List Supplier that will return an appropriate WireMock Instance for a given Service ID
 */
class TestServiceInstanceListSupplier(private val serviceId: String) : ServiceInstanceListSupplier {

    override fun getServiceId() = serviceId

    override fun get(): Flux<List<ServiceInstance>> =
            Flux.just(listOf(
                    DefaultServiceInstance("$serviceId 1", serviceId, "localhost", RoutesTest.serviceIdToServerMap[serviceId]?.port()!!, false)
            ))
}

/**
 * InstanceListSupplierProvider that always returns the local test InstanceListSupplier
 */
class TestServiceInstanceListSupplierProvider(serviceId: String) : ObjectProvider<ServiceInstanceListSupplier> {

    private val testSupplier = TestServiceInstanceListSupplier(serviceId)

    override fun getIfUnique() = testSupplier

    override fun getObject(vararg args: Any?) = testSupplier

    override fun getObject() = testSupplier

    override fun getIfAvailable() = testSupplier
}