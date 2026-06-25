package com.marcoscode.elearning.student;

import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.enrollment.Enrollment;
import com.marcoscode.elearning.enrollment.EnrollmentRepository;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.security.SecurityService;
import com.marcoscode.elearning.student.dto.StudentCreateDto;
import com.marcoscode.elearning.student.dto.StudentResponseDto;
import com.marcoscode.elearning.student.dto.StudentUpdateDto;
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
public class StudentService {
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentMappingDto studentMappingDto;
    private final UserRepository userRepository;
    private final SecurityService securityService;


    @PreAuthorize(
            "hasRole('ADMIN')"
    )
    public Page<StudentResponseDto> getStudents(Pageable pageable) {
        Page<Student> studentPage = studentRepository.findAll(pageable);
        List<Student> students = studentPage.getContent();

        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .toList();

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdIn(studentIds);

//        System.out.println("Student IDs: " + studentIds);
//        System.out.println("Enrollments found: " + enrollments.size());
//
//        enrollments.forEach(e ->
//                System.out.println(
//                        "Student: " + e.getStudent().getId()
//                                + " Course: " + e.getCourse().getId()
//                )
//        );

        Map<Long, List<Course>> courseByStudent = enrollments.stream()
                .collect(Collectors.groupingBy(enrollment ->
                        enrollment.getStudent().getId(),
                        Collectors.mapping(Enrollment::getCourse, Collectors.toList())));

        List<StudentResponseDto> responseDtos = students.stream()
                .map(student -> {
                    List<Course> courses = courseByStudent.getOrDefault(student.getId(), List.of());
                    return studentMappingDto.toStudentResponseDto(student, courses);
                })
                .toList();

        return new PageImpl<>(responseDtos, pageable, studentPage.getTotalElements());
    }

    @PreAuthorize(
            "hasRole('ADMIN')"
    )
    public StudentResponseDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("student not found"));
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdIn(List.of(student.getId()));

        List<Course> courses = enrollments.stream()
                .map(Enrollment::getCourse)
                .toList();
        return studentMappingDto.toStudentResponseDto(student, courses);
    }

    @PreAuthorize(
            "hasRole('STUDENT')"
    )
    public StudentResponseDto getMyOwnProfile() {

        Long studentId = securityService.getCurrentUserId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("student not found"));
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdIn(List.of(student.getId()));

        List<Course> courses = enrollments.stream()
                .map(Enrollment::getCourse)
                .toList();
        return studentMappingDto.toStudentResponseDto(student, courses);

    }

    public StudentResponseDto createStudent(@Valid StudentCreateDto createDto) {
        Student student = studentMappingDto.createFromDto(createDto);

        if (createDto.email() != null) {
            String email = createDto.email().trim();

            userRepository.findByEmail(email)
                    .ifPresent(user -> {
                        throw new IllegalStateException("email already exist");
                    });
        }

        studentRepository.save(student);

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdIn(List.of(student.getId()));

        List<Course> courses = enrollments.stream()
                .map(Enrollment::getCourse)
                .toList();

        return studentMappingDto.toStudentResponseDto(student, courses);
    }

    @Transactional
    @PreAuthorize(
            "hasRole('STUDENT')"
    )

    public StudentResponseDto updateMyProfile(@Valid StudentUpdateDto updateDto) {
        Long studentId = securityService.getCurrentUserId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("student not found"));

        if (updateDto.email() != null) {
            String email = updateDto.email().trim();

            userRepository.findByEmail(email)
                    .ifPresent(user -> {
                        throw new IllegalStateException("email already exist");
                    });
        }

        studentMappingDto.updateFromDto(student, updateDto);

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdIn(List.of(student.getId()));

        List<Course> courses = enrollments.stream()
                .map(Enrollment::getCourse)
                .toList();

        return studentMappingDto.toStudentResponseDto(student, courses);
    }

}
