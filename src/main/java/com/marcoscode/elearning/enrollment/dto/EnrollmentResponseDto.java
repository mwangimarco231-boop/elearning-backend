package com.marcoscode.elearning.enrollment.dto;

import java.time.LocalDateTime;

public record EnrollmentResponseDto(
        Long enrollmentId,

        Long studentId,
        String studentName,

        Long courseId,
        String courseTitle,

        LocalDateTime enrollmentDate

) {
}
