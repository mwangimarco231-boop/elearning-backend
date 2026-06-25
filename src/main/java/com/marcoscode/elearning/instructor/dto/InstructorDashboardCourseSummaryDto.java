package com.marcoscode.elearning.instructor.dto;

import com.marcoscode.elearning.course.Level;

import java.time.LocalDateTime;

public record InstructorDashboardCourseSummaryDto(
        Long id,
        String title,
        Double price,
        Level courseLevel,
        long studentCount,
        LocalDateTime createdAt
) {
}
