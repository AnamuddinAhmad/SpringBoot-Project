package com.auth.Auth.security;

import com.auth.Auth.entity.Roles;
import com.auth.Auth.entity.Users;
import com.auth.Auth.exception.ValidationError;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
@Setter
public class JWTServices {

    private final SecretKey key;
    private final long accessTTLSecond;
    private final long refreshTTLSecond;
    private final String issuer;

    public JWTServices(

            @Value("${security.jwt.secret}") String secret,

            @Value("${security.jwt.access-token-expiration}")
            long accessTTLSecond,

            @Value("${security.jwt.refresh-token-expiration}")
            long refreshTTLSecond,

            @Value("${security.jwt.issuer}")
            String issuer
    ) {
        if (secret == null || secret.length() < 64){
            throw  ValidationError.forbidden("Invalid Secret Key.");
        }

        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.accessTTLSecond = accessTTLSecond;
        this.refreshTTLSecond = refreshTTLSecond;
        this.issuer = issuer;
    }

    //Genrate Token.
    public String genrateAccessToken(Users users){
        Instant now = Instant.now();
        List<String> roles = users.getRoles() == null ? List.of() :
                users.getRoles().stream().map(Roles::getName).toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(users.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTTLSecond)))
                .claims(Map.of("email",users.getEmail(),"roles",roles, "typ", "access"))
                .signWith(key)

                .compact();
    }

    public String genrateRefreshToken(Users users,String jti){
        Instant now = Instant.now();
        List<String> roles = users.getRoles() == null ? List.of() :
                users.getRoles().stream().map(Roles::getName).toList();
        return Jwts.builder()
                .id(jti)
                .subject(users.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTTLSecond)))
                .claim("typ","refresh")
                .signWith(key,SignatureAlgorithm.HS512)
                .compact();
    }

    public Jws<Claims> parse(String token){
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        }catch (JwtException error){
            System.out.println("Error while parsing the JWT tokens.");
            throw new ValidationError(error.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    public boolean isAccessToken(String token){
        Claims c = parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public boolean isRefreshToken(String token){
        Claims c = parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserId(String token){
        Claims c = parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }

    public String getJti(String token){
        return parse(token).getPayload().getId();
    }

    public List<String> getRoles(String token){
        Claims c = parse(token).getPayload();
        return (List<String>) c.get("roles");
    }

    public String getEmail(String token){
        Claims c = parse(token).getPayload();
        return (String) c.get("email");
    }
}