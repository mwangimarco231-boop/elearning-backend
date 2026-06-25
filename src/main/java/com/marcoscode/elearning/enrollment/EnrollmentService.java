package com.marcoscode.elearning.enrollment;

import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.course.CourseRepository;
import com.marcoscode.elearning.enrollment.dto.EnrollmentCreateDto;
import com.marcoscode.elearning.enrollment.dto.EnrollmentResponseDto;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.security.SecurityService;
import com.marcoscode.elearning.student.Student;
import com.marcoscode.elearning.student.StudentRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final SecurityService securityService;



    @PreAuthorize("hasRole('ADMIN')")
    public Page<EnrollmentResponseDto> getEnrollments(Pageable pageable) {
        Page<Enrollment> enrollmentPage = enrollmentRepository.findAll(pageable);
        List<Enrollment> enrollments = enrollmentPage.getContent();

        List<EnrollmentResponseDto> responseDtos = enrollments.stream()
                .map(enrollmentMapper::toEnrollmentResponse)
                .toList();

        return new PageImpl<>(responseDtos, pageable, enrollmentPage.getTotalElements());
    }

    @PreAuthorize("hasRole('STUDENT')")
    public Page<EnrollmentResponseDto> getMyEnrollments(Pageable pageable) {

        Long studentId = securityService.getCurrentUserId();
        Page<Enrollment> enrollmentsPage = enrollmentRepository.findByStudentId(studentId, pageable);

        return enrollmentsPage
                .map(enrollmentMapper::toEnrollmentResponse);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public EnrollmentResponseDto getEnrollmentsById(Long id) {
        return enrollmentRepository.findById(id).
                map(enrollmentMapper::toEnrollmentResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));
    }


    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public EnrollmentResponseDto createEnrollment(EnrollmentCreateDto enrollmentCreateDto) {

        Long studentId = securityService.getCurrentUserId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(()-> new ResourceNotFoundException("Student profile not found"));

        Course course = courseRepository.findById(enrollmentCreateDto.courseId())
                .orElseThrow(()-> new ResourceNotFoundException("course not found"));


        if (enrollmentRepository.existsByStudentIdAndCourseId(
                student.getId(),
                course.getId())
        ) {
            throw new IllegalStateException("student and course already exist");
        }

        Enrollment enrollment = Enrollment.builder()
                .build();
        enrollment.linkStudentAndCourse(student, course);

        enrollmentRepository.save(enrollment);

        return enrollmentMapper.toEnrollmentResponse(enrollment);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEnrollment(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("enrollment not found"));

        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();

        enrollment.unlinkStudentAndCourse(student, course);

        enrollmentRepository.delete(enrollment);
    }

}
