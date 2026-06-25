package com.marcoscode.elearning.instructor.dto;

import com.marcoscode.elearning.course.Level;

public record InstructorPublicCoursesSummaryDto(
        Long id,
        String title,
        double price,
        Level courseLevel
) {
}
