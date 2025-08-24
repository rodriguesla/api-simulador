package com.github.rodriguesla.api_simulador.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_USERNAME = "testuser";
    private static final String INVALID_TOKEN = "invalid.jwt.token";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void generateTokenShouldCreateValidToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.contains("."));
    }

    @Test
    void generateTokenShouldContainCorrectUsername() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void generateTokenShouldHaveCorrectClaims() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        Claims claims = jwtUtil.extractClaim(token, claims1 -> claims1);

        assertEquals("api-simulador", claims.get("iss"));
        assertEquals("hackathon-2025", claims.get("aud"));
        assertEquals(TEST_USERNAME, claims.getSubject());
    }

    @Test
    void extractExpirationShouldReturnFutureDate() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        Date expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateTokenShouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        Boolean isValid = jwtUtil.validateToken(token, TEST_USERNAME);

        assertTrue(isValid);
    }

    @Test
    void validateTokenShouldReturnTrueForValidTokenWithoutUsername() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        Boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateTokenShouldReturnFalseForWrongUsername() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        Boolean isValid = jwtUtil.validateToken(token, "wronguser");

        assertFalse(isValid);
    }

    @Test
    void validateTokenShouldReturnFalseForInvalidToken() {
        Boolean isValid = jwtUtil.validateToken(INVALID_TOKEN);

        assertFalse(isValid);
    }

    @Test
    void validateTokenShouldReturnFalseForNullToken() {
        Boolean isValid = jwtUtil.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    void validateTokenShouldReturnFalseForEmptyToken() {
        Boolean isValid = jwtUtil.validateToken("");

        assertFalse(isValid);
    }

    @Test
    void extractUsernameShouldThrowExceptionForInvalidToken() {
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(INVALID_TOKEN);
        });
    }

    @Test
    void extractExpirationShouldThrowExceptionForInvalidToken() {
        assertThrows(Exception.class, () -> {
            jwtUtil.extractExpiration(INVALID_TOKEN);
        });
    }

    @Test
    void differentUsernamesShouldGenerateDifferentTokens() {
        String user1 = "user1";
        String user2 = "user2";
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        assertNotEquals(token1, token2);
        assertEquals(user1, jwtUtil.extractUsername(token1));
        assertEquals(user2, jwtUtil.extractUsername(token2));
    }

    @Test
    void sameUsernameShouldGenerateDifferentTokensAtDifferentTimes() throws InterruptedException {
        String token1 = jwtUtil.generateToken(TEST_USERNAME);
        Thread.sleep(1000);
        String token2 = jwtUtil.generateToken(TEST_USERNAME);

        assertNotEquals(token1, token2);
        assertEquals(TEST_USERNAME, jwtUtil.extractUsername(token1));
        assertEquals(TEST_USERNAME, jwtUtil.extractUsername(token2));
    }

    @Test
    void extractClaimShouldWorkWithCustomClaimExtractor() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        String issuer = jwtUtil.extractClaim(token, claims -> (String) claims.get("iss"));
        String audience = jwtUtil.extractClaim(token, claims -> (String) claims.get("aud"));

        assertEquals("api-simulador", issuer);
        assertEquals("hackathon-2025", audience);
    }

    @Test
    void validateTokenShouldReturnFalseForMalformedToken() {
        String malformedToken = "malformed.jwt.token.with.invalid.structure";
        Boolean isValid = jwtUtil.validateToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    void validateTokenShouldHandleNullTokenGracefully() {
        Boolean isValid = jwtUtil.validateToken((String) null);

        assertFalse(isValid);
    }

    @Test
    void validateTokenShouldHandleTokenWithInvalidSignature() {
        String validToken = jwtUtil.generateToken(TEST_USERNAME);
        String tokenWithInvalidSignature = validToken.substring(0, validToken.length() - 10) + "invalidsig";

        Boolean isValid = jwtUtil.validateToken(tokenWithInvalidSignature);

        assertFalse(isValid);
    }

    @Test
    void validateTokenShouldReturnFalseWhenExceptionIsThrown() {
        String malformedToken = "not.a.jwt";
        assertFalse(jwtUtil.validateToken(malformedToken));
        assertFalse(jwtUtil.validateToken(malformedToken, "anyuser"));
        assertFalse(jwtUtil.validateToken(null));
        assertFalse(jwtUtil.validateToken(null, "anyuser"));
        assertFalse(jwtUtil.validateToken("", "anyuser"));
    }
}
