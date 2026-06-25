package com.marcoscode.elearning.course;



import com.marcoscode.elearning.course.dto.CourseCreateDto;
import com.marcoscode.elearning.course.dto.CourseResponseDto;
import com.marcoscode.elearning.course.dto.CourseUpdateDto;
import com.marcoscode.elearning.course.dto.CourseUpdateInstructorIdDto;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.instructor.Instructor;
import com.marcoscode.elearning.instructor.InstructorRepository;
import com.marcoscode.elearning.security.SecurityService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapping courseMapping;
    private final InstructorRepository instructorRepository;
    private final SecurityService securityService;


    public Page<CourseResponseDto> getCourses(Pageable pageable) {
        Page<Course> coursePage = courseRepository.findAll(pageable);
        List<Course> courseList = coursePage.getContent();

        List<CourseResponseDto> responseDto = courseList
                .stream()
                .map(courseMapping::toCourseResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(responseDto, pageable, coursePage.getTotalElements());
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public Page<CourseResponseDto> myCourses(Pageable pageable) {

        Long instructorId = securityService.getCurrentUser().getId();

        Page<Course> coursesPage = courseRepository.findByInstructorId(instructorId, pageable);

        return coursesPage.map(courseMapping::toCourseResponseDto);
    }

    public CourseResponseDto getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .map(courseMapping::toCourseResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Course with id:" + courseId + " does not exists"));
    }

    @Transactional
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'INSTRUCTOR')"
    )
    public CourseResponseDto createCourse(CourseCreateDto courseCreateDto) {

        Instructor instructor;
        boolean isAdmin = securityService
                .getCurrentUser()
                .getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));


        if(isAdmin) {
            if (courseCreateDto.instructorId() == null)
            {
                throw new IllegalArgumentException(
                        "Administrators must provide a valid instructor id to asssing this course.");
            }

            instructor = instructorRepository.findById(courseCreateDto.instructorId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Instructor with id:" + courseCreateDto.instructorId() + " not found"));
        } else {
            Long instructorId = securityService.getCurrentUser().getId();

            instructor = instructorRepository.findById(instructorId)
                    .orElseThrow(() -> new IllegalStateException("Instructor profile not found"));
        }

        if (courseRepository.existsByTitleAndInstructorIdAndCourseLevel(
                courseCreateDto.title(),
                instructor.getId(),
                courseCreateDto.courseLevel())){
            throw new IllegalStateException("You have already created a course named: " + courseCreateDto.title());
        }

        var course = courseMapping.createFromDto(courseCreateDto);
        instructor.addCourse(course);

        Course savedCourse = courseRepository.save(course);
        return courseMapping.toCourseResponseDto(savedCourse);
    }


    @Transactional
    @PreAuthorize(
            "hasRole('ADMIN') || @securityService.ownsCourse(#courseId)"
    )
    public CourseResponseDto updateCourse(Long courseId, CourseUpdateDto courseUpdateDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("course with id:" + courseId + " does not exists"));

        Level inboundLevel = courseUpdateDto.courseLevel();

        if(courseRepository.existsByTitleAndInstructorIdAndCourseLevel(courseUpdateDto.title(),courseId,inboundLevel)){
                throw new  IllegalStateException("You already have a course with this title at " + inboundLevel + "level");
        }

        courseMapping.updateCourseFromDto(course, courseUpdateDto);
        return courseMapping.toCourseResponseDto(course);
    }


    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CourseResponseDto updateCourseInstructorId(
            Long courseId,
            CourseUpdateInstructorIdDto courseUpdateInstructorIdDto
    ) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("course with id:" + courseId + " does not exists"));

        if (courseRepository.existsByTitleAndInstructorIdAndCourseLevel(
                course.getTitle(),
                courseUpdateInstructorIdDto.newInstructorId(),
                course.getCourseLevel())) {
            throw new  IllegalStateException("Course exists with same instructor id: " + courseUpdateInstructorIdDto.newInstructorId());
        }

        Instructor newInstructor = instructorRepository.findById(courseUpdateInstructorIdDto.newInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException
                        ("instructor with id:" + courseUpdateInstructorIdDto.newInstructorId() + " does not exists"));

        if (course.getInstructor() != null) {
            course.getInstructor().removeCourse(course);
        }
        newInstructor.addCourse(course);

        return courseMapping.toCourseResponseDto(course);
    }

    @Transactional
    @PreAuthorize(
            "hasRole('ADMIN') || @securityService.ownsCourse(#courseId)"
    )
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("course with id:" + courseId + " does not exists"));

        courseRepository.delete(course);
    }
}