package com.marcoscode.elearning.instructor;


import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.course.CourseRepository;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.instructor.dto.InstructorCreateDto;
import com.marcoscode.elearning.instructor.dto.InstructorDashboardDto;
import com.marcoscode.elearning.instructor.dto.InstructorPublicResponseDto;
import com.marcoscode.elearning.instructor.dto.InstructorUpdateDto;
import com.marcoscode.elearning.security.SecurityService;
import com.marcoscode.elearning.user.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final InstructorMapper instructorMapper;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    public Page<InstructorPublicResponseDto> getInstructors(Pageable pageable) {
        Page<Instructor> instructorPage = instructorRepository.findAll(pageable);
        List<Instructor> instructors = instructorPage.getContent();

        List<Long> instructorIds = instructors.stream()
                .map(Instructor::getId)
                .toList();
        List<Course> courses = courseRepository.findByInstructorIdIn(instructorIds);

        Map<Long, List<Course>> courseByInstructor = courses.stream()
                .collect(Collectors.groupingBy(course -> course.getInstructor().getId()));

        List<InstructorPublicResponseDto> responseDtos = instructors.stream()
                .map(instructor -> {
                    List<Course> instructorCourse = courseByInstructor.getOrDefault(instructor.getId(), List.of());
                    return instructorMapper.toInstructorPublicResponseDto(instructor, instructorCourse);
                })
                .toList();

        return new PageImpl<>(responseDtos, pageable, instructorPage.getTotalElements());
    }

    public InstructorPublicResponseDto getInstructorById(Long id) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Instructor with id " + id + " not found"));

        List<Course> courses = courseRepository.findByInstructorIdIn(List.of(instructor.getId()));
        return instructorMapper.toInstructorPublicResponseDto(instructor, courses);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public InstructorDashboardDto getMyProfile() {
        Long instructorId = securityService.getCurrentUser().getId();

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor with id " + instructorId + " not found"));

        List<Course> courses =  courseRepository.findByInstructorIdIn(List.of(instructorId));
        return instructorMapper.toInstructorResponseDto(instructor, courses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public InstructorPublicResponseDto registerInstructor(@Valid InstructorCreateDto createDto) {

        if(createDto.email() != null) {
            String email = createDto.email().trim();

            userRepository.findByEmail(email)
                    .ifPresent(u -> {
                        throw new IllegalStateException("Email already exists");
                    });
        }

        Instructor instructor = instructorMapper.createFromDto(createDto);
        instructorRepository.save(instructor);
        List<Course> courses = courseRepository.findByInstructorIdIn(List.of(instructor.getId()));

        return instructorMapper.toInstructorPublicResponseDto(instructor, courses);
    }


    @Transactional
    @PreAuthorize(
                "hasRole('INSTRUCTOR')"
    )
    public InstructorPublicResponseDto updateMyProfile(@Valid InstructorUpdateDto updateDto) {

        Long instructorId = securityService.getCurrentUserId();

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(()-> new ResourceNotFoundException("Instructor with id " + instructorId + " not found"));

        if (updateDto.email() != null) {
            String email = updateDto.email().trim();
            userRepository.findByEmail(email)
                    .filter(u -> !u.getId().equals(instructorId))
                    .ifPresent(u -> {
                        throw new IllegalStateException("This email address is already in use by another account.");
                    });

        }

        instructorMapper.updateFromDto(instructor, updateDto);

        List<Course> courses = courseRepository.findByInstructorIdIn(List.of(instructorId));

        return instructorMapper.toInstructorPublicResponseDto(instructor, courses);
    }
}
