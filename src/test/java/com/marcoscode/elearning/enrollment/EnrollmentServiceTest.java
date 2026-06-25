package com.marcoscode.elearning.enrollment;

import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.course.CourseRepository;
import com.marcoscode.elearning.enrollment.dto.EnrollmentCreateDto;
import com.marcoscode.elearning.enrollment.dto.EnrollmentResponseDto;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.security.SecurityService;
import com.marcoscode.elearning.student.Student;
import com.marcoscode.elearning.student.StudentRepository;
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
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Nested
    @DisplayName("Get Enrollments Tests")
    class GetEnrollmentsTests {

        @Test
        @DisplayName("Should return paginated enrollments")
        void shouldReturnPaginatedEnrollments() {

            Enrollment enrollment = Enrollment.builder().build();

            EnrollmentResponseDto responseDto =
                    mock(EnrollmentResponseDto.class);

            Pageable pageable = PageRequest.of(0, 10);

            Page<Enrollment> page =
                    new PageImpl<>(List.of(enrollment), pageable, 1);

            when(enrollmentRepository.findAll(pageable))
                    .thenReturn(page);

            when(enrollmentMapper.toEnrollmentResponse(enrollment))
                    .thenReturn(responseDto);

            Page<EnrollmentResponseDto> result =
                    enrollmentService.getEnrollments(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(responseDto, result.getContent().get(0));

            verify(enrollmentRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no enrollments exist")
        void shouldReturnEmptyEnrollmentsPage() {

            Pageable pageable = PageRequest.of(0, 10);

            when(enrollmentRepository.findAll(pageable))
                    .thenReturn(Page.empty(pageable));

            Page<EnrollmentResponseDto> result =
                    enrollmentService.getEnrollments(pageable);

            assertTrue(result.isEmpty());

            verifyNoInteractions(enrollmentMapper);
        }
    }

    @Nested
    @DisplayName("Get My Enrollments Tests")
    class GetMyEnrollmentsTests {

        @Test
        @DisplayName("Should return current student's enrollments")
        void shouldReturnCurrentStudentEnrollments() {

            Long studentId = 1L;

            Enrollment enrollment = Enrollment.builder().build();

            EnrollmentResponseDto responseDto =
                    mock(EnrollmentResponseDto.class);

            Pageable pageable = PageRequest.of(0, 10);

            Page<Enrollment> page =
                    new PageImpl<>(List.of(enrollment), pageable, 1);

            when(securityService.getCurrentUserId())
                    .thenReturn(studentId);

            when(enrollmentRepository.findByStudentId(studentId, pageable))
                    .thenReturn(page);

            when(enrollmentMapper.toEnrollmentResponse(enrollment))
                    .thenReturn(responseDto);

            Page<EnrollmentResponseDto> result =
                    enrollmentService.getMyEnrollments(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(responseDto, result.getContent().get(0));
        }

        @Test
        @DisplayName("Should return empty page when student has no enrollments")
        void shouldReturnEmptyPageForStudent() {

            Long studentId = 1L;

            Pageable pageable = PageRequest.of(0, 10);

            when(securityService.getCurrentUserId())
                    .thenReturn(studentId);

            when(enrollmentRepository.findByStudentId(studentId, pageable))
                    .thenReturn(Page.empty(pageable));

            Page<EnrollmentResponseDto> result =
                    enrollmentService.getMyEnrollments(pageable);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Enrollment By Id Tests")
    class GetEnrollmentByIdTests {

        @Test
        @DisplayName("Should return enrollment when found")
        void shouldReturnEnrollmentById() {

            Long enrollmentId = 1L;

            Enrollment enrollment = Enrollment.builder()
                    .id(enrollmentId)
                    .build();

            EnrollmentResponseDto responseDto =
                    mock(EnrollmentResponseDto.class);

            when(enrollmentRepository.findById(enrollmentId))
                    .thenReturn(Optional.of(enrollment));

            when(enrollmentMapper.toEnrollmentResponse(enrollment))
                    .thenReturn(responseDto);

            EnrollmentResponseDto result =
                    enrollmentService.getEnrollmentsById(enrollmentId);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should throw exception when enrollment not found")
        void shouldThrowWhenEnrollmentMissing() {

            when(enrollmentRepository.findById(1L))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception =
                    assertThrows(
                            ResourceNotFoundException.class,
                            () -> enrollmentService.getEnrollmentsById(1L)
                    );

            assertEquals(
                    "Enrollment not found with id: 1",
                    exception.getMessage()
            );
        }
    }

    @Nested
    @DisplayName("Create Enrollment Tests")
    class CreateEnrollmentTests {

        @Test
        @DisplayName("Should create enrollment successfully")
        void shouldCreateEnrollmentSuccessfully() {

            Long studentId = 1L;
            Long courseId = 2L;

            Student student = Student.builder()
                    .id(studentId)
                    .build();

            Course course = Course.builder()
                    .id(courseId)
                    .build();

            EnrollmentCreateDto createDto =
                    new EnrollmentCreateDto(courseId);

            EnrollmentResponseDto responseDto =
                    mock(EnrollmentResponseDto.class);

            when(securityService.getCurrentUserId())
                    .thenReturn(studentId);

            when(studentRepository.findById(studentId))
                    .thenReturn(Optional.of(student));

            when(courseRepository.findById(courseId))
                    .thenReturn(Optional.of(course));

            when(enrollmentRepository.existsByStudentIdAndCourseId(
                    studentId,
                    courseId))
                    .thenReturn(false);

            when(enrollmentMapper.toEnrollmentResponse(any(Enrollment.class)))
                    .thenReturn(responseDto);

            EnrollmentResponseDto result =
                    enrollmentService.createEnrollment(createDto);

            assertNotNull(result);

            verify(enrollmentRepository)
                    .save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should throw exception when student profile does not exist")
        void shouldThrowWhenStudentMissing() {

            Long studentId = 1L;

            EnrollmentCreateDto createDto =
                    new EnrollmentCreateDto(2L);

            when(securityService.getCurrentUserId())
                    .thenReturn(studentId);

            when(studentRepository.findById(studentId))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception =
                    assertThrows(
                            ResourceNotFoundException.class,
                            () -> enrollmentService.createEnrollment(createDto)
                    );

            assertEquals(
                    "Student profile not found",
                    exception.getMessage()
            );

            verifyNoInteractions(courseRepository);
        }

        @Test
        @DisplayName("Should throw exception when course does not exist")
        void shouldThrowWhenCourseMissing() {

            Long studentId = 1L;

            Student student = Student.builder()
                    .id(studentId)
                    .build();

            EnrollmentCreateDto createDto =
                    new EnrollmentCreateDto(99L);

            when(securityService.getCurrentUserId())
                    .thenReturn(studentId);

            when(studentRepository.findById(studentId))
                    .thenReturn(Optional.of(student));

            when(courseRepository.findById(99L))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception =
                    assertThrows(
                            ResourceNotFoundException.class,
                            () -> enrollmentService.createEnrollment(createDto)
                    );

            assertEquals(
                    "course not found",
                    exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should throw exception when enrollment already exists")
        void shouldThrowWhenEnrollmentAlreadyExists() {

            Long studentId = 1L;
            Long courseId = 2L;

            Student student = Student.builder()
                    .id(studentId)
                    .build();

            Course course = Course.builder()
                    .id(courseId)
                    .build();

            EnrollmentCreateDto createDto =
                    new EnrollmentCreateDto(courseId);

            when(securityService.getCurrentUserId())
                    .thenReturn(studentId);

            when(studentRepository.findById(studentId))
                    .thenReturn(Optional.of(student));

            when(courseRepository.findById(courseId))
                    .thenReturn(Optional.of(course));

            when(enrollmentRepository.existsByStudentIdAndCourseId(
                    studentId,
                    courseId))
                    .thenReturn(true);

            IllegalStateException exception =
                    assertThrows(
                            IllegalStateException.class,
                            () -> enrollmentService.createEnrollment(createDto)
                    );

            assertEquals(
                    "student and course already exist",
                    exception.getMessage()
            );

            verify(enrollmentRepository, never())
                    .save(any());
        }
    }

    @Nested
    @DisplayName("Delete Enrollment Tests")
    class DeleteEnrollmentTests {

        @Test
        @DisplayName("Should delete enrollment successfully")
        void shouldDeleteEnrollmentSuccessfully() {

            Student student = Student.builder()
                    .id(1L)
                    .build();

            Course course = Course.builder()
                    .id(2L)
                    .build();

            Enrollment enrollment = Enrollment.builder()
                    .student(student)
                    .course(course)
                    .build();

            when(enrollmentRepository.findById(1L))
                    .thenReturn(Optional.of(enrollment));

            assertDoesNotThrow(
                    () -> enrollmentService.deleteEnrollment(1L)
            );

            verify(enrollmentRepository)
                    .delete(enrollment);
        }

        @Test
        @DisplayName("Should throw exception when enrollment does not exist")
        void shouldThrowWhenDeletingMissingEnrollment() {

            when(enrollmentRepository.findById(1L))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception =
                    assertThrows(
                            ResourceNotFoundException.class,
                            () -> enrollmentService.deleteEnrollment(1L)
                    );

            assertEquals(
                    "enrollment not found",
                    exception.getMessage()
            );

            verify(enrollmentRepository, never())
                    .delete(any());
        }
    }
}