package com.marcoscode.elearning.user;


import com.marcoscode.elearning.user.dto.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {


    public UserResponseDto toUserResponseDto(User user) {
        return new  UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName());
    }
}
