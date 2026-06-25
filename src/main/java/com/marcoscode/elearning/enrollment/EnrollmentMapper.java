package com.marcoscode.elearning.enrollment;


import com.marcoscode.elearning.enrollment.dto.EnrollmentResponseDto;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentResponseDto toEnrollmentResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        Long studentId = enrollment.getStudent() != null ? enrollment.getStudent().getId() : null;
        Long courseId = enrollment.getCourse() != null ? enrollment.getCourse().getId() : null;

        String courseTitle = enrollment.getCourse() != null ? enrollment.getCourse().getTitle() : null;

        String studentName = String.format(
                "%s %s",
                enrollment.getStudent() != null ? enrollment.getStudent().getFirstName() : null,
                enrollment.getStudent() != null ? enrollment.getStudent().getLastName() : null
        );

        return new EnrollmentResponseDto(
                enrollment.getId(),
                studentId,
                studentName,
                courseId,
                courseTitle,
                enrollment.getEnrollmentDate()
        );
    }
}
