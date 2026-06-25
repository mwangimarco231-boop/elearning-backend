package com.marcoscode.elearning.auth;


import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;


    public void createRefreshToken(
            User user,
            String token,
            LocalDateTime expiresAt
    ){
        RefreshToken refreshTokens = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiredAt(expiresAt)
                .revoked(false)
                .build();

         refreshTokenRepository.save(refreshTokens);
    }

    public RefreshToken getByToken(String token){
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh not found"));
    }


    public boolean isValid(RefreshToken token){
        return !token.isRevoked()
                && token.getExpiredAt().isAfter(LocalDateTime.now());
    }

    public void revokeToken(RefreshToken refreshToken){
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }


}
