package com.marcoscode.elearning.auth;

import com.marcoscode.elearning.auth.dto.*;
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
    public ResponseEntity<AuthenticationResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequest
    ){
        return ResponseEntity.ok(
                authService.login(loginRequest)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(
            @RequestBody RefreshTokenRequestDto requestDto
    ){
        return ResponseEntity.ok(authService.refreshToken(requestDto));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserRepositoryDto> me(){
        return ResponseEntity.ok(authService.getCurrentUser());
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody LogoutRequestDto requestDto

    ){
        authService.logout(requestDto);
        return ResponseEntity.ok().build();
    }
}
