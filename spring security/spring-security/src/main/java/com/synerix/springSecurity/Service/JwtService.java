package com.synerix.springSecurity.Service;

import com.synerix.springSecurity.Model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    // Secret key used for JWT signing and validation
    private static final String SECRET_KEY = "cac744364aad3fc234f43b24c76e5666b42d7a7040715270aa37cb7d854d0691";

    // Extracts username from JWT token
    public String extractUserName(String token){
        return extractClaim(token, Claims::getSubject);
    }

    // Checks if the given JWT token is valid for the provided user details
    public boolean isValid(String token, UserDetails user){
        String username = extractUserName(token);
        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

    // Checks if the JWT token has expired
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    // Extracts expiration date from JWT token
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    // Extracts a specific claim from JWT token using a resolver function
    public <T> T extractClaim(String token, Function<Claims,T> resolver){
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // Extracts all claims from JWT token
    private Claims extractAllClaims(String token){
        return Jwts
                .parser()
                .verifyWith(getSigningKey()) // Verifies token with the signing key
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Generates a JWT token for the provided user
    public String generateToken(User user){
        String token = Jwts
                .builder()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis())) // Token issued at current time
                .expiration(new Date(System.currentTimeMillis() + 24*60*60*1000)) // Token expiration set to 24 hours from now
                .signWith(getSigningKey()) // Signs token with the signing key
                .compact();
        return token;
    }

    // Retrieves the signing key for JWT operations
    private SecretKey getSigningKey(){
        byte[] keyBytes = Decoders.BASE64URL.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes); // Creates an HMAC SHA key from the decoded bytes
    }
}
