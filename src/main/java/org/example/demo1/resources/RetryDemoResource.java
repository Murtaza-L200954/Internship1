package org.example.demo1.resources;

import org.example.demo1.common.ResilientProductService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/demo")
public class RetryDemoResource {

    private final ResilientProductService resilientService = new ResilientProductService();

    @GET
    @Path("/resilient")
    @Produces(MediaType.TEXT_PLAIN)
    public String callWithCircuitBreakerAndRetry() {
        return resilientService.getProductWithResilience();
    }
}
