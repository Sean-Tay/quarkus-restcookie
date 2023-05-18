package org.acme

import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*
import jakarta.ws.rs.container.*
import jakarta.ws.rs.ext.Provider
import jakarta.enterprise.context.ApplicationScoped

import org.jboss.resteasy.reactive.*
import org.jboss.resteasy.reactive.server.*

const val RESOURCE_PATH = "/api"

@NameBinding
@Retention(AnnotationRetention.RUNTIME)
annotation class JwtCookieUpserterBinder

@ApplicationScoped
@Provider
class JwtCookieUpserter {
  @JwtCookieUpserterBinder
  @ServerRequestFilter
  fun filterRequest(requestContext: ContainerRequestContext?) {
    var jwtCookie = requestContext?.cookies?.get("jwt")
    if (jwtCookie == null || jwtCookie.value.isNullOrEmpty()) {
      val newJwt: String = "jwt-value"
      jwtCookie = NewCookie("jwt", newJwt, RESOURCE_PATH, null, null, 600, false)
      requestContext?.headers?.add(HttpHeaders.COOKIE, jwtCookie.toString()) // this no longer seems to have any effect in ApiResource
      requestContext?.setProperty("set-jwt", jwtCookie)
    }
    requestContext?.setProperty("jwt", jwtCookie)
  }

  @JwtCookieUpserterBinder
  @ServerResponseFilter
  fun filterResponse(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
    val jwtCookie = requestContext?.getProperty("set-jwt")
    if (jwtCookie != null) {
      responseContext?.headers?.add(HttpHeaders.SET_COOKIE, jwtCookie.toString())
    }
  }
}

@JwtCookieUpserterBinder
@ApplicationScoped
@Path(RESOURCE_PATH)
class ApiResource {
  @Inject
  lateinit var requestContext: ContainerRequestContext

  // ...

  private fun getJwtCookieValueFromRequestContext(): String {
    return (requestContext.getProperty("jwt") as? Cookie)?.value ?: "Not Set"
  }

  // Route meant for Testing
  @GET
  @Path("/jwt")
  @Produces(MediaType.TEXT_PLAIN)
  fun getJwt(
    @RestCookie("jwt") jwt: String? // This still seems necessary for the Endpoint to take in the User-Agent's Cookie Value if they provide it in the Request
  ): Response {
    // val jwtCookieValue = getJwtCookieValueFromRequestContext()
    return Response.ok(jwt, MediaType.TEXT_PLAIN).build()
  }

  // ... other Resource Endpoints ...
}