package com.marcoscode.elearning.user;

import com.marcoscode.elearning.course.CourseRepository;
import com.marcoscode.elearning.exception.BadRequestException;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.instructor.InstructorRepository;
import com.marcoscode.elearning.user.dto.UserResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("Get Users Tests")
    class GetUsersTests {

        @Test
        @DisplayName("Should return list of UserResponseDto when users exist")
        void shouldReturnListOfUsers() {
            // Arrange
            User user1 = User.builder().id(1L).build();
            User user2 = User.builder().id(2L).build();

            UserResponseDto dto1 = new UserResponseDto(
                    1L,
                    "john@example.com",
                    "John",
                    "Ken");
            UserResponseDto dto2 = new UserResponseDto(
                    2L,
                    "jane@example.com",
                    "Mary",
                    "Jane");

            when(userRepository.findAll()).thenReturn(List.of(user1, user2));
            when(userMapper.toUserResponseDto(user1)).thenReturn(dto1);
            when(userMapper.toUserResponseDto(user2)).thenReturn(dto2);

            // Act
            List<UserResponseDto> result = userService.getUsers();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(dto1, result.getFirst());

            // Verify
            verify(userRepository, times(1)).findAll();
            verify(userMapper, times(1)).toUserResponseDto(user1);
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsers() {
            // Arrange
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<UserResponseDto> result = userService.getUsers();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return UserResponseDto when valid ID is provided")
        void shouldReturnUserWhenIdExists() {
            // Arrange
            Long userId = 1L;
            User mockUser = User.builder().id(userId).build();

            UserResponseDto expectedResponse = new UserResponseDto(
                    1L,
                    "john@example.com",
                    "John",
                    "Ken");


            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userMapper.toUserResponseDto(mockUser)).thenReturn(expectedResponse);

            // Act
            UserResponseDto result = userService.getUserById(userId);

            // Assert
            assertNotNull(result);
            assertEquals(userId, result.id());

            // Verify
            verify(userRepository, times(1)).findById(userId);
            verify(userMapper, times(1)).toUserResponseDto(mockUser);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user ID does not exist")
        void shouldThrowExceptionWhenIdDoesNotExist() {
            // Arrange
            Long userId = 99L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> userService.getUserById(userId));

            assertEquals("User not found", exception.getMessage());

            // Verify
            verify(userRepository, times(1)).findById(userId);
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("Delete User By ID Tests")
    class DeleteUserByIdTests {

        @Test
        @DisplayName("Should delete user successfully if user is a standard student profile")
        void shouldDeleteStandardUserSuccessfully() {
            // Arrange
            Long userId = 1L;
            User mockUser = User.builder().id(userId).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            // Simulate that this user profile does not exist in the instructor registry table
            when(instructorRepository.existsById(userId)).thenReturn(false);

            // Act and Assert
            assertDoesNotThrow(() -> userService.deleteUserById(userId));

            // Verify
            verify(userRepository, times(1)).delete(mockUser);
            verifyNoInteractions(courseRepository); // Optimization validation path skipped cleanly
        }

        @Test
        @DisplayName("Should delete user successfully if they are an instructor with no assigned courses")
        void shouldDeleteInstructorWithNoCourses() {
            // Arrange
            Long userId = 2L;
            User mockUser = User.builder().id(userId).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(instructorRepository.existsById(userId)).thenReturn(true);
            when(courseRepository.existsByInstructorId(userId)).thenReturn(false); // No dependencies found

            // Act and Assert
            assertDoesNotThrow(() -> userService.deleteUserById(userId));

            // Assert
            verify(userRepository, times(1)).delete(mockUser);
            verify(instructorRepository, times(1)).existsById(userId);
            verify(courseRepository, times(1)).existsByInstructorId(userId);
        }

        @Test
        @DisplayName("Should throw BadRequestException when trying to delete an instructor who still owns active courses")
        void shouldThrowBadRequestExceptionWhenInstructorHasCourses() {
            // Arrange
            Long userId = 3L;
            User mockUser = User.builder().id(userId).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(instructorRepository.existsById(userId)).thenReturn(true);
            when(courseRepository.existsByInstructorId(userId)).thenReturn(true); // Flag active dependency block

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> userService.deleteUserById(userId));

            assertEquals("Cannot delete instructor. Please reassign their courses first.", exception.getMessage());
            verify(userRepository, never()).delete(any(User.class)); // Safety verification
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException early if user entity is completely missing")
        void shouldThrowExceptionWhenDeletingMissingUser() {
            // Arrange
            Long userId = 99L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> userService.deleteUserById(userId));

            // Verify
            verify(userRepository).findById(userId);
            verifyNoInteractions(instructorRepository, courseRepository);
            verify(userRepository, never()).delete(any());
        }
    }
}
