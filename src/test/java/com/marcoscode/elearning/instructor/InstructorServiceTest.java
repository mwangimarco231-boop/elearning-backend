package com.marcoscode.elearning.instructor;

import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.course.CourseRepository;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.instructor.dto.InstructorCreateDto;
import com.marcoscode.elearning.instructor.dto.InstructorDashboardDto;
import com.marcoscode.elearning.instructor.dto.InstructorPublicResponseDto;
import com.marcoscode.elearning.instructor.dto.InstructorUpdateDto;
import com.marcoscode.elearning.security.CustomUserDetails;
import com.marcoscode.elearning.security.SecurityService;
import com.marcoscode.elearning.user.User;
import com.marcoscode.elearning.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InstructorMapper instructorMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private InstructorService instructorService;

    @Nested
    @DisplayName("Get Instructor By Id Tests")
    class GetInstructorByIdTests {

        @Test
        @DisplayName("Should return instructor when found")
        void shouldReturnInstructorById() {

            Long id = 1L;

            Instructor instructor = Instructor.builder()
                    .id(id)
                    .build();

            List<Course> courses = List.of(
                    Course.builder().id(10L).build()
            );

            InstructorPublicResponseDto dto =
                    mock(InstructorPublicResponseDto.class);

            when(instructorRepository.findById(id))
                    .thenReturn(Optional.of(instructor));

            when(courseRepository.findByInstructorIdIn(List.of(id)))
                    .thenReturn(courses);

            when(instructorMapper.toInstructorPublicResponseDto(
                    instructor,
                    courses))
                    .thenReturn(dto);

            InstructorPublicResponseDto result =
                    instructorService.getInstructorById(id);

            assertNotNull(result);

            verify(instructorRepository).findById(id);
        }

        @Test
        @DisplayName("Should throw exception when instructor does not exist")
        void shouldThrowWhenInstructorMissing() {

            when(instructorRepository.findById(1L))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex =
                    assertThrows(
                            ResourceNotFoundException.class,
                            () -> instructorService.getInstructorById(1L)
                    );

            assertEquals(
                    "Instructor with id 1 not found",
                    ex.getMessage()
            );
        }
    }

    @Nested
    @DisplayName("Get My Profile Tests")
    class GetMyProfileTests {

        @Test
        @DisplayName("Should return current instructor profile")
        void shouldReturnCurrentInstructorProfile() {

            Long instructorId = 1L;

            Instructor instructor = Instructor.builder()
                    .id(instructorId)
                    .build();


            InstructorDashboardDto dto =
                    mock(InstructorDashboardDto.class);


            CustomUserDetails mockUser = mock(CustomUserDetails.class);

            when(mockUser.getId()).thenReturn(instructorId);
            when(securityService.getCurrentUser()).thenReturn(mockUser);

            when(instructorRepository.findById(instructorId)).thenReturn(Optional.of(instructor));

            when(courseRepository.findByInstructorIdIn(List.of(instructorId))).thenReturn(List.of());

            when(instructorMapper.toInstructorResponseDto(eq(instructor), anyList())).thenReturn(dto);

            InstructorDashboardDto result =
                    instructorService.getMyProfile();

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should throw exception when current instructor profile is missing")
        void shouldThrowWhenCurrentInstructorMissing() {

            Long targetInstructorId = 1L;
            CustomUserDetails mockUser = mock(CustomUserDetails.class);

            when(mockUser.getId()).thenReturn(targetInstructorId);
            when(securityService.getCurrentUser()).thenReturn(mockUser);
            when(instructorRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> instructorService.getMyProfile()
            );
        }
    }

    @Nested
    @DisplayName("Register Instructor Tests")
    class RegisterInstructorTests {

        @Test
        @DisplayName("Should register instructor successfully")
        void shouldRegisterInstructorSuccessfully() {

            InstructorCreateDto createDto =
                    mock(InstructorCreateDto.class);

            when(createDto.email())
                    .thenReturn("john@test.com");

            Instructor instructor =
                    Instructor.builder()
                            .id(1L)
                            .build();

            InstructorPublicResponseDto response =
                    mock(InstructorPublicResponseDto.class);

            when(userRepository.findByEmail("john@test.com"))
                    .thenReturn(Optional.empty());

            when(instructorMapper.createFromDto(createDto))
                    .thenReturn(instructor);

            when(instructorMapper.toInstructorPublicResponseDto(
                    eq(instructor),
                    anyList()))
                    .thenReturn(response);

            InstructorPublicResponseDto result =
                    instructorService.registerInstructor(createDto);

            assertNotNull(result);

            verify(instructorRepository).save(instructor);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowWhenEmailAlreadyExists() {

            InstructorCreateDto createDto =
                    mock(InstructorCreateDto.class);

            when(createDto.email())
                    .thenReturn("john@test.com");

            when(userRepository.findByEmail("john@test.com"))
                    .thenReturn(Optional.of(User.builder().build()));

            IllegalStateException ex =
                    assertThrows(
                            IllegalStateException.class,
                            () -> instructorService.registerInstructor(createDto)
                    );

            assertEquals(
                    "Email already exists",
                    ex.getMessage()
            );

            verify(instructorRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update instructor profile successfully")
        void shouldUpdateProfileSuccessfully() {

            Long instructorId = 1L;

            Instructor instructor =
                    Instructor.builder()
                            .id(instructorId)
                            .build();

            InstructorUpdateDto dto =
                    mock(InstructorUpdateDto.class);

            when(dto.email())
                    .thenReturn("new@test.com");

            InstructorPublicResponseDto response =
                    mock(InstructorPublicResponseDto.class);

            when(securityService.getCurrentUserId())
                    .thenReturn(instructorId);

            when(instructorRepository.findById(instructorId))
                    .thenReturn(Optional.of(instructor));

            when(userRepository.findByEmail("new@test.com"))
                    .thenReturn(Optional.empty());

            when(instructorMapper.toInstructorPublicResponseDto(
                    eq(instructor),
                    anyList()))
                    .thenReturn(response);

            InstructorPublicResponseDto result =
                    instructorService.updateMyProfile(dto);

            assertNotNull(result);

            verify(instructorMapper)
                    .updateFromDto(instructor, dto);
        }

        @Test
        @DisplayName("Should throw exception when instructor does not exist")
        void shouldThrowWhenInstructorMissing() {

            when(securityService.getCurrentUserId())
                    .thenReturn(1L);

            when(instructorRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> instructorService.updateMyProfile(
                            mock(InstructorUpdateDto.class)
                    )
            );
        }

        @Test
        @DisplayName("Should throw exception when email belongs to another user")
        void shouldThrowWhenEmailBelongsToAnotherUser() {

            Long instructorId = 1L;

            Instructor instructor =
                    Instructor.builder()
                            .id(instructorId)
                            .build();

            InstructorUpdateDto dto =
                    mock(InstructorUpdateDto.class);

            when(dto.email())
                    .thenReturn("taken@test.com");

            User existingUser =
                    User.builder()
                            .id(99L)
                            .build();

            when(securityService.getCurrentUserId())
                    .thenReturn(instructorId);

            when(instructorRepository.findById(instructorId))
                    .thenReturn(Optional.of(instructor));

            when(userRepository.findByEmail("taken@test.com"))
                    .thenReturn(Optional.of(existingUser));

            IllegalStateException ex =
                    assertThrows(
                            IllegalStateException.class,
                            () -> instructorService.updateMyProfile(dto)
                    );

            assertEquals(
                    "This email address is already in use by another account.",
                    ex.getMessage()
            );

            verify(instructorMapper, never())
                    .updateFromDto(any(), any());
        }

        @Test
        @DisplayName("Should allow update when email belongs to current instructor")
        void shouldAllowUpdatingWhenEmailBelongsToCurrentInstructor() {

            Long instructorId = 1L;

            Instructor instructor =
                    Instructor.builder()
                            .id(instructorId)
                            .build();

            User sameUser =
                    User.builder()
                            .id(instructorId)
                            .build();

            InstructorUpdateDto dto =
                    mock(InstructorUpdateDto.class);

            when(dto.email())
                    .thenReturn("same@test.com");

            when(securityService.getCurrentUserId())
                    .thenReturn(instructorId);

            when(instructorRepository.findById(instructorId))
                    .thenReturn(Optional.of(instructor));

            when(userRepository.findByEmail("same@test.com"))
                    .thenReturn(Optional.of(sameUser));

            when(instructorMapper.toInstructorPublicResponseDto(
                    eq(instructor),
                    anyList()))
                    .thenReturn(mock(InstructorPublicResponseDto.class));

            assertDoesNotThrow(
                    () -> instructorService.updateMyProfile(dto)
            );

            verify(instructorMapper)
                    .updateFromDto(instructor, dto);
        }
    }

    @Nested
    @DisplayName("Get Instructors Tests")
    class GetInstructorsTests {

        @Test
        @DisplayName("Should return empty page when no instructors exist")
        void shouldReturnEmptyInstructorPage() {

            Pageable pageable = PageRequest.of(0, 10);

            when(instructorRepository.findAll(pageable))
                    .thenReturn(Page.empty(pageable));

            Page<InstructorPublicResponseDto> result =
                    instructorService.getInstructors(pageable);

            assertTrue(result.isEmpty());

            verifyNoInteractions(instructorMapper);
        }
    }
}