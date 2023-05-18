package org.acme

import jakarta.ws.rs.GET
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*
import jakarta.ws.rs.container.*
import jakarta.ws.rs.ext.Provider

const val RESOURCE_PATH = "/api"

@NameBinding
@Retention(AnnotationRetention.RUNTIME)
annotation class JwtCookieUpserterBinder

@JwtCookieUpserterBinder
@Provider
class JwtCookieUpserter: ContainerRequestFilter, ContainerResponseFilter {
  override fun filter(requestContext: ContainerRequestContext) {
    var jwtCookie = requestContext?.cookies?.get("jwt")
    if (jwtCookie == null || jwtCookie.value.isNullOrEmpty()) {
      val newJwt: String = "jwt-value"
      jwtCookie = NewCookie("jwt", newJwt, RESOURCE_PATH, null, null, 600, false)
      requestContext?.headers?.add(HttpHeaders.COOKIE, jwtCookie.toString())
      requestContext?.setProperty("jwt", jwtCookie)
    }
  }

  override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
    val jwtCookie = requestContext?.getProperty("jwt")
    if (jwtCookie != null) {
      responseContext?.headers?.add(HttpHeaders.SET_COOKIE, jwtCookie.toString())
    }
  }
}

@JwtCookieUpserterBinder
@Path(RESOURCE_PATH)
class ApiResource {
  // ...

  // Route meant for Testing
  @GET
  @Path("/jwt")
  @Produces(MediaType.TEXT_PLAIN)
  fun getJwt(
    @CookieParam("jwt") jwtCookieValue: String?
  ): Response {
    return Response.ok(jwtCookieValue, MediaType.TEXT_PLAIN).build()
  }

  // ... other Resource Endpoints ...
}