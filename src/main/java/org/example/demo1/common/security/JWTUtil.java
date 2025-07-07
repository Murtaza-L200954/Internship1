package org.example.demo1.common.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;

public class JWTUtil {

    private static final String SECRET;

    static {
        String envSecret = System.getenv("JWT_SECRET_KEY");
        if (envSecret == null || envSecret.length() < 32) {
            throw new IllegalStateException("Environment variable JWT_SECRET_KEY must be set and have at least 32 characters.");
        }
        SECRET = envSecret;
    }

    private static final long EXPIRATION_TIME = 1000 * 60 * 30; // 30 minutes

    public static String generateToken(String username, String role) {
        try {
            JWSSigner signer = new MACSigner(SECRET);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("role", role)
                    .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .issueTime(new Date())
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );
            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean validateToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SECRET);
            return jwt.verify(verifier) && !isTokenExpired(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JWTClaimsSet getClaimsSet(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isTokenExpired(SignedJWT jwt) {
        try {
            Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
            return expirationTime.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public static String extractRoleFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;

        String token = authHeader.substring("Bearer ".length()).trim();

        if (!validateToken(token)) return null;

        try {
            JWTClaimsSet claims = getClaimsSet(token);
            return claims.getStringClaim("role");
        } catch (Exception e) {
            return null;
        }
    }

}
