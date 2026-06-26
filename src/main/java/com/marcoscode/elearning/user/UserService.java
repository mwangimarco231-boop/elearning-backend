package com.marcoscode.elearning.user;


import com.marcoscode.elearning.course.CourseRepository;
import com.marcoscode.elearning.exception.BadRequestException;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.instructor.InstructorRepository;
import com.marcoscode.elearning.user.dto.UserResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;



    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto getUserById(Long userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        return userMapper.toUserResponseDto(targetUser);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserById(Long userId) {
    User targetUser = userRepository.findById(userId)
            .orElseThrow(()-> new ResourceNotFoundException("User not found"));


    if (instructorRepository.existsById(userId)) {
        boolean hasCourses = courseRepository.existsByInstructorId(userId);

        if(hasCourses){
            throw new BadRequestException("Cannot delete instructor. Please reassign their courses first.");
        }
    }

    userRepository.delete(targetUser);
    }
}
