package com.example.auth.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationInMillis;

    public String generateToken(UUID userId, String email, String role) {
        try {
            // 1. Creăm semnătura HMAC cu secretul nostru
            JWSSigner signer = new MACSigner(secret);

            // 2. Pregătim claim-urile (informațiile din token)
            Instant now = Instant.now();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(email)                         // "sub": email-ul utilizatorului
                    .claim("userId", userId.toString())     // "userId": ID-ul
                    .claim("role", role)                    // "role": Rolul (CLIENT/ADMIN)
                    .issueTime(Date.from(now))              // "iat": momentul emiterii
                    .expirationTime(Date.from(now.plusMillis(expirationInMillis))) // "exp": expirarea
                    .build();

            // 3. Creăm obiectul JWT semnat (Header + Payload)
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            // 4. Aplicăm semnătura
            signedJWT.sign(signer);

            // 5. Returnăm token-ul serializat (string-ul lung)
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("Eroare la generarea token-ului JWT", e);
        }
    }
}
