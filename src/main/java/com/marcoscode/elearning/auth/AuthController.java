package com.marcoscode.elearning.auth;

import com.marcoscode.elearning.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "404", description = "User not found")}
    )
    public ResponseEntity<AuthenticationResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequest
    ){
        return ResponseEntity.ok(
                authService.login(loginRequest)
        );
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generates a new JWT access token and refresh token using a valid refresh token. The old refresh token is revoked." )

    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Refresh token is invalid or expired"),
            @ApiResponse(responseCode = "404", description = "Refresh token not found") })
    public ResponseEntity<AuthenticationResponseDto> refreshToken(
            @RequestBody RefreshTokenRequestDto requestDto
    ){
        return ResponseEntity.ok(authService.refreshToken(requestDto));
    }



    @GetMapping("/me")

    @Operation(
            summary = "Get current authenticated user",
            description = "Returns information about the currently authenticated user based on the JWT access token." )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required or access token is invalid") })

    public ResponseEntity<CurrentUserRepositoryDto> me(){
        return ResponseEntity.ok(authService.getCurrentUser());
    }




    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Logs out the current user by revoking the supplied refresh token. The access token will naturally expire after its configured lifetime." )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Refresh token is invalid"),
            @ApiResponse(responseCode = "404", description = "Refresh token not found") })

    public ResponseEntity<Void> logout(
            @RequestBody LogoutRequestDto requestDto

    ){
        authService.logout(requestDto);
        return ResponseEntity.ok().build();
    }
}
