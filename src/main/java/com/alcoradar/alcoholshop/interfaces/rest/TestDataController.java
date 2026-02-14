package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.application.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Test data controller for BCrypt password hashing.
 * <p>
 * This controller is ONLY for development/testing to work around BCrypt issues.
 * It should be removed or disabled in production.
 * </p>
 *
 * @see SecurityService
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@Tag(name = "test", description = "Test data endpoints (development only)")
public class TestDataController {

    private final SecurityService securityService;

    /**
     * Generate a BCrypt hash for a given password.
     * <p>
     * Useful for generating hashes to manually insert into database.
     * </p>
     *
     * @param password plain text password
     * @return ResponseEntity containing BCrypt hash
     */
    @GetMapping("/hash")
    @Operation(
            summary = "Generate BCrypt hash",
            description = """
                    Generates a BCrypt hash for a given password.
                    Useful for debugging and creating test data.
                    """
    )
    @ApiResponse(responseCode = "200", description = "BCrypt hash generated")
    public ResponseEntity<?> generateHash(@RequestParam String password) {
        String hash = securityService.hashPassword(password);
        log.info("Generated BCrypt hash for password: {} -> {} (length: {})",
                password, hash, hash.length());
        return ResponseEntity.ok(new HashResponse(hash, hash.length()));
    }

    /**
     * Test BCrypt password validation.
     * <p>
     * Use this to verify that a given password matches a given BCrypt hash.
     * </p>
     *
     * @param password plain text password
     * @param hash      BCrypt hash to test against
     * @return ResponseEntity containing match result
     */
    @PostMapping("/validate")
    @Operation(
            summary = "Test BCrypt validation",
            description = """
                    Tests if a given password matches a given BCrypt hash.
                    Useful for debugging password validation issues.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Validation test completed")
    public ResponseEntity<?> testValidation(@RequestParam String password, @RequestParam String hash) {
        boolean matches = securityService.checkPassword(password, hash);
        log.info("Password validation test: password='{}', hash='{}' -> matches={}",
                password, hash, matches);
        return ResponseEntity.ok(new ValidationResponse(matches));
    }

    private record HashResponse(String hash, int length) {}
    private record ValidationResponse(boolean matches) {}
}
