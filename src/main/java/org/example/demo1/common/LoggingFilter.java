package org.example.demo1.common;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import org.example.demo1.common.security.JWTUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null || authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if(JWTUtil.validateToken(token)){
                JWTClaimsSet claims = JWTUtil.getClaimsSet(token);
                if(claims != null){
                    MDC.put("userId", claims.getSubject());
                    try{
                        MDC.put("role",claims.getStringClaim("role"));
                    } catch(java.text.ParseException e){
                        MDC.put("role","unknown");
                    }
                }
                else{
                    MDC.put("userId","anonymous");
                    MDC.put("role","public");
                }
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        MDC.clear();
    }
}
