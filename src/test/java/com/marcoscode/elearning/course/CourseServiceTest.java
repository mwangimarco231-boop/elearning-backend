package com.marcoscode.elearning.course;

import com.marcoscode.elearning.course.dto.CourseCreateDto;
import com.marcoscode.elearning.course.dto.CourseResponseDto;
import com.marcoscode.elearning.course.dto.CourseUpdateDto;
import com.marcoscode.elearning.course.dto.CourseUpdateInstructorIdDto;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.instructor.Instructor;
import com.marcoscode.elearning.instructor.InstructorRepository;
import com.marcoscode.elearning.security.CustomUserDetails;
import com.marcoscode.elearning.security.SecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseMapping courseMapping;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private CourseService courseService;

    @Nested
    @DisplayName("Get & Read Courses Tests")
    class ReadCoursesTests {

        @Test
        @DisplayName("Should return a paginated layout of all course records")
        void getCourses_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Course mockCourse = Course.builder().id(1L).title("Spring Boot Dev").build();
            Page<Course> mockPage = new PageImpl<>(List.of(mockCourse), pageable, 1);

            CourseResponseDto mockDto = new CourseResponseDto(
                    1L,
                    "Spring Boot Dev",
                    29.99,
                    Level.BEGINNER,
                    5L,
                    "Instructor",
                    0,
                    LocalDateTime.now()
            );


            when(courseRepository.findAll(pageable)).thenReturn(mockPage);
            when(courseMapping.toCourseResponseDto(mockCourse)).thenReturn(mockDto);

            Page<CourseResponseDto> result = courseService.getCourses(pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Spring Boot Dev", result.getContent().getFirst().title());
        }

        @Test
        @DisplayName("Should fetch active instructor restricted course lists")
        void myCourses_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            CustomUserDetails mockUser = mock(CustomUserDetails.class);
            Course mockCourse = Course.builder().id(1L).title("Java Advanced").build();
            Page<Course> mockPage = new PageImpl<>(List.of(mockCourse), pageable, 1);

            CourseResponseDto mockDto = new CourseResponseDto(
                    1L,
                    "Java Advanced",
                    29.99, Level.ADVANCED,
                    5L,
                    "System Instructor",
                    0,
                    LocalDateTime.now()
            );

            when(securityService.getCurrentUser()).thenReturn(mockUser);
            when(mockUser.getId()).thenReturn(5L);
            when(courseRepository.findByInstructorId(5L, pageable)).thenReturn(mockPage);
            when(courseMapping.toCourseResponseDto(mockCourse)).thenReturn(mockDto);

            Page<CourseResponseDto> result = courseService.myCourses(pageable);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(courseRepository).findByInstructorId(5L, pageable);
        }

        @Test
        @DisplayName("Should return target details when matching course ID exists")
        void getCourseById_Success() {
            Long courseId = 1L;
            Course mockCourse = Course.builder().id(courseId).title("Git Basics").build();
            CourseResponseDto mockDto = new CourseResponseDto(
                    courseId,
                    "Git Basics",
                    23.3,
                    Level.BEGINNER,
                    5L,
                    "System Instructor",
                    0,
                    LocalDateTime.now()
            );

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
            when(courseMapping.toCourseResponseDto(mockCourse)).thenReturn(mockDto);

            CourseResponseDto result = courseService.getCourseById(courseId);

            assertNotNull(result);
            assertEquals("Git Basics", result.title());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when query ID is missing")
        void getCourseById_NotFound() {
            Long courseId = 99L;
            when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(courseId));
        }
    }

    @Nested
    @DisplayName("Create Course Tests")
    class CreateCourseTests {

        @Test
        @DisplayName("Should create course successfully when executed by an Instructor profile context")
        void createCourse_AsInstructor_Success() {

            CourseCreateDto createDto = new CourseCreateDto(
                    "Docker Basics",
                    87.3,
                    Level.BEGINNER,
                    null);

            CustomUserDetails mockUser = mock(CustomUserDetails.class);
            Instructor instructor = Instructor.builder().id(5L).courses(new ArrayList<>()).build();
            Course course = Course.builder().title("Docker Basics").courseLevel(Level.BEGINNER).build();

            CourseResponseDto outDto = new CourseResponseDto(
                    1L,
                    "Docker Basics",
                    23.2,
                    Level.BEGINNER,
                    3L,
                    "Instructor",
                    0,
                    LocalDateTime.now());

            when(securityService.getCurrentUser()).thenReturn(mockUser);
            when(mockUser.getAuthorities()).thenReturn(new ArrayList<>()); // No admin role
            when(mockUser.getId()).thenReturn(5L);
            when(instructorRepository.findById(5L)).thenReturn(Optional.of(instructor));
            when(courseRepository.existsByTitleAndInstructorIdAndCourseLevel("Docker Basics", 5L, Level.BEGINNER)).thenReturn(false);
            when(courseMapping.createFromDto(createDto)).thenReturn(course);
            when(courseRepository.save(course)).thenReturn(course);
            when(courseMapping.toCourseResponseDto(course)).thenReturn(outDto);

            CourseResponseDto result = courseService.createCourse(createDto);

            assertNotNull(result);
            verify(courseRepository).save(course);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if Administrator fails to define an instructor target ID")
        void createCourse_AsAdmin_MissingInstructorId_ThrowsError() {

            CourseCreateDto createDto = new CourseCreateDto(
                    "Docker Basics",
                    87.3,
                    Level.BEGINNER,
                    null);
            CustomUserDetails mockUser = mock(CustomUserDetails.class);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

            when(securityService.getCurrentUser()).thenReturn(mockUser);
            doReturn(authorities).when(mockUser).getAuthorities();

            assertThrows(IllegalArgumentException.class, () -> courseService.createCourse(createDto));
        }
    }

    @Nested
    @DisplayName("Update & Modify Course Tests")
    class UpdateCourseTests {

        @Test
        @DisplayName("Should save modifications safely when no unique parameters conflict")
        void updateCourse_Success() {
            Long courseId = 1L;
            CourseUpdateDto updateDto = new CourseUpdateDto(
                    "Updated Title",
                    Level.INTERMEDIATE,
                    33.2);

            Instructor instructor = Instructor.builder().id(5L).build();
            Course currentCourse = Course.builder().id(courseId).title("Old Title").courseLevel(Level.BEGINNER).instructor(instructor).build();
            CourseResponseDto expectedDto = new CourseResponseDto(
                    courseId,
                    "Updated Title",
                    12.4,
                    Level.INTERMEDIATE,
                    1L,
                    "Instructor",
                    0,
                    LocalDateTime.now());

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(currentCourse));
            // 💡 Note: Using lenient/relaxed rules to account for whichever method variation you use (fixed or legacy)
            when(courseRepository.existsByTitleAndInstructorIdAndCourseLevel(anyString(), anyLong(), any())).thenReturn(false);
            when(courseMapping.toCourseResponseDto(currentCourse)).thenReturn(expectedDto);

            CourseResponseDto result = courseService.updateCourse(courseId, updateDto);

            assertNotNull(result);
            assertEquals("Updated Title", result.title());
            verify(courseMapping).updateCourseFromDto(currentCourse, updateDto);
        }

        @Test
        @DisplayName("Should change primary teacher assignments cleanly when executed by Admin permissions")
        void updateCourseInstructorId_Success() {
            Long courseId = 1L;
            CourseUpdateInstructorIdDto updateInstructorDto = new CourseUpdateInstructorIdDto(12L);
            Instructor oldInstructor = Instructor.builder().id(5L).courses(new ArrayList<>()).build();
            Instructor newInstructor = Instructor.builder().id(12L).courses(new ArrayList<>()).build();
            Course currentCourse = Course.builder().id(courseId).title("Kubernetes Core").courseLevel(Level.ADVANCED).instructor(oldInstructor).build();

            oldInstructor.addCourse(currentCourse);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(currentCourse));
            when(courseRepository.existsByTitleAndInstructorIdAndCourseLevel("Kubernetes Core", 12L, Level.ADVANCED)).thenReturn(false);
            when(instructorRepository.findById(12L)).thenReturn(Optional.of(newInstructor));

            courseService.updateCourseInstructorId(courseId, updateInstructorDto);

            assertFalse(oldInstructor.getCourses().contains(currentCourse));
            assertTrue(newInstructor.getCourses().contains(currentCourse));
            assertEquals(newInstructor, currentCourse.getInstructor());
        }
    }

    @Nested
    @DisplayName("Delete Course Tests")
    class DeleteCourseTests {

        @Test
        @DisplayName("Should delete course")
        void deleteCourse_Success() {
            Long courseId = 1L;

            Course mockCourse = Course.builder().id(courseId).build();

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));

            courseService.deleteCourse(courseId);

            verify(courseRepository).delete(mockCourse);
            verifyNoMoreInteractions(courseRepository);
            verifyNoInteractions(courseMapping);
        }
    }


}

