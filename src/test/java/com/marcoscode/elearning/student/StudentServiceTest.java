package com.marcoscode.elearning.student;

import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.enrollment.Enrollment;
import com.marcoscode.elearning.enrollment.EnrollmentRepository;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.security.SecurityService;
import com.marcoscode.elearning.student.dto.StudentCreateDto;
import com.marcoscode.elearning.student.dto.StudentResponseDto;
import com.marcoscode.elearning.student.dto.StudentUpdateDto;
import com.marcoscode.elearning.user.User;
import com.marcoscode.elearning.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentMappingDto studentMappingDto;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private StudentService studentService;

    @Nested
    @DisplayName("Get Students Tests")
    class GetStudentsTests {

        @Test
        @DisplayName("Should return paged student responses")
        void shouldReturnStudentsPage() {

            Student student = Student.builder()
                    .id(1L)
                    .build();

            Course course = Course.builder()
                    .id(10L)
                    .title("Java")
                    .build();

            Enrollment enrollment = Enrollment.builder()
                    .student(student)
                    .course(course)
                    .build();

            StudentResponseDto dto = mock(StudentResponseDto.class);

            Pageable pageable = PageRequest.of(0, 10);

            Page<Student> page =
                    new PageImpl<>(List.of(student), pageable, 1);

            when(studentRepository.findAll(pageable))
                    .thenReturn(page);

            when(enrollmentRepository.findByStudentIdIn(List.of(1L)))
                    .thenReturn(List.of(enrollment));

            when(studentMappingDto.toStudentResponseDto(
                    eq(student),
                    anyList()))
                    .thenReturn(dto);

            Page<StudentResponseDto> result =
                    studentService.getStudents(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(dto, result.getContent().get(0));

            verify(studentRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no students exist")
        void shouldReturnEmptyPage() {

            Pageable pageable = PageRequest.of(0, 10);

            when(studentRepository.findAll(pageable))
                    .thenReturn(Page.empty(pageable));

            Page<StudentResponseDto> result =
                    studentService.getStudents(pageable);

            assertTrue(result.isEmpty());

            verifyNoInteractions(studentMappingDto);
        }
    }

    @Nested
    @DisplayName("Get Student By Id Tests")
    class GetStudentByIdTests {

        @Test
        @DisplayName("Should return student when found")
        void shouldReturnStudentById() {

            Long studentId = 1L;

            Student student = Student.builder()
                    .id(studentId)
                    .build();

            Course course = Course.builder()
                    .id(1L)
                    .build();

            Enrollment enrollment = Enrollment.builder()
                    .student(student)
                    .course(course)
                    .build();

            StudentResponseDto dto = mock(StudentResponseDto.class);

            when(studentRepository.findById(studentId))
                    .thenReturn(Optional.of(student));

            when(enrollmentRepository.findByStudentIdIn(List.of(studentId)))
                    .thenReturn(List.of(enrollment));

            when(studentMappingDto.toStudentResponseDto(
                    eq(student),
                    anyList()))
                    .thenReturn(dto);

            StudentResponseDto result =
                    studentService.getStudentById(studentId);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should throw exception when student does not exist")
        void shouldThrowWhenStudentMissing() {

            when(studentRepository.findById(1L))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception =
                    assertThrows(
                            ResourceNotFoundException.class,
                            () -> studentService.getStudentById(1L)
                    );

            assertEquals("student not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get My Own Profile Tests")
    class GetMyOwnProfileTests {

        @Test
        @DisplayName("Should return current student profile")
        void shouldReturnCurrentStudentProfile() {

            Long studentId = 1L;

            Student student = Student.builder()
                    .id(studentId)
                    .build();

            StudentResponseDto dto = mock(StudentResponseDto.class);

            when(securityService.getCurrentUserId())
                    .thenReturn(studentId);

            when(studentRepository.findById(studentId))
                    .thenReturn(Optional.of(student));

            when(enrollmentRepository.findByStudentIdIn(List.of(studentId)))
                    .thenReturn(List.of());

            when(studentMappingDto.toStudentResponseDto(
                    eq(student),
                    anyList()))
                    .thenReturn(dto);

            StudentResponseDto result =
                    studentService.getMyOwnProfile();

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should throw when current student profile does not exist")
        void shouldThrowWhenCurrentStudentMissing() {

            when(securityService.getCurrentUserId())
                    .thenReturn(1L);

            when(studentRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> studentService.getMyOwnProfile()
            );
        }
    }

    @Nested
    @DisplayName("Create Student Tests")
    class CreateStudentTests {

        @Test
        @DisplayName("Should create student successfully")
        void shouldCreateStudentSuccessfully() {

            StudentCreateDto dto = mock(StudentCreateDto.class);

            when(dto.email())
                    .thenReturn("john@example.com");

            Student student = Student.builder()
                    .id(1L)
                    .build();

            StudentResponseDto response =
                    mock(StudentResponseDto.class);

            when(studentMappingDto.createFromDto(dto))
                    .thenReturn(student);

            when(userRepository.findByEmail("john@example.com"))
                    .thenReturn(Optional.empty());

            when(studentMappingDto.toStudentResponseDto(
                    eq(student),
                    anyList()))
                    .thenReturn(response);

            StudentResponseDto result =
                    studentService.createStudent(dto);

            assertNotNull(result);

            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("Should throw when email already exists")
        void shouldThrowWhenEmailAlreadyExists() {

            StudentCreateDto dto = mock(StudentCreateDto.class);

            when(dto.email())
                    .thenReturn("john@example.com");

            when(studentMappingDto.createFromDto(dto))
                    .thenReturn(Student.builder().build());

            User existingUser = User.builder().build();

            when(userRepository.findByEmail("john@example.com"))
                    .thenReturn(Optional.of(existingUser));

            IllegalStateException exception =
                    assertThrows(
                            IllegalStateException.class,
                            () -> studentService.createStudent(dto)
                    );

            assertEquals(
                    "email already exist",
                    exception.getMessage()
            );

            verify(studentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update current student profile")
        void shouldUpdateProfileSuccessfully() {

            Long studentId = 1L;

            Student student = Student.builder()
                    .id(studentId)
                    .build();

            StudentUpdateDto dto = mock(StudentUpdateDto.class);

            when(dto.email())
                    .thenReturn("new@email.com");

            StudentResponseDto response =
                    mock(StudentResponseDto.class);

            when(securityService.getCurrentUserId())
                    .thenReturn(studentId);

            when(studentRepository.findById(studentId))
                    .thenReturn(Optional.of(student));

            when(userRepository.findByEmail("new@email.com"))
                    .thenReturn(Optional.empty());

            when(studentMappingDto.toStudentResponseDto(
                    eq(student),
                    anyList()))
                    .thenReturn(response);

            StudentResponseDto result =
                    studentService.updateMyProfile(dto);

            assertNotNull(result);

            verify(studentMappingDto)
                    .updateFromDto(student, dto);
        }

        @Test
        @DisplayName("Should throw when updating missing student")
        void shouldThrowWhenUpdatingMissingStudent() {

            when(securityService.getCurrentUserId())
                    .thenReturn(1L);

            when(studentRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> studentService.updateMyProfile(
                            mock(StudentUpdateDto.class)
                    )
            );
        }

        @Test
        @DisplayName("Should throw when email already exists")
        void shouldThrowWhenUpdateEmailAlreadyExists() {

            Long studentId = 1L;

            Student student = Student.builder()
                    .id(studentId)
                    .build();

            StudentUpdateDto dto = mock(StudentUpdateDto.class);

            when(dto.email())
                    .thenReturn("taken@email.com");

            when(securityService.getCurrentUserId())
                    .thenReturn(studentId);

            when(studentRepository.findById(studentId))
                    .thenReturn(Optional.of(student));

            when(userRepository.findByEmail("taken@email.com"))
                    .thenReturn(Optional.of(User.builder().build()));

            IllegalStateException exception =
                    assertThrows(
                            IllegalStateException.class,
                            () -> studentService.updateMyProfile(dto)
                    );

            assertEquals(
                    "email already exist",
                    exception.getMessage()
            );

            verify(studentMappingDto, never())
                    .updateFromDto(any(), any());
        }
    }
}