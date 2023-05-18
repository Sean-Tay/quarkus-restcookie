package org.acme

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class ApiResourceTest {
    @Test
    fun testEndpoint() {
        given()
            .`when`().get("/api/jwt")
            .then()
                .statusCode(200)
                .body(`is`("jwt-value"))
    }

    @Test
    fun testEndpointWithCookie() {
        given()
            .header("Cookie", "jwt=another-jwt-value")
            .`when`().get("/api/jwt")
            .then()
                .statusCode(200)
                .body(`is`("another-jwt-value"))
    }
}