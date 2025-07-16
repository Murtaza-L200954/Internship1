package org.example.demo1.common;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log =  LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable throwable) {
        log.error("Unhandled exception occurred", throwable);
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("An internal server error occurred")
                .build();
    }
}
