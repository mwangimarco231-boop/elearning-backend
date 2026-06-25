package com.marcoscode.elearning.course.dto;

import com.marcoscode.elearning.course.Level;

import java.time.LocalDateTime;

public record CourseResponseDto(
        Long courseId,
        String title,
        Double price,
        Level courseLevel,
        Long instructorId,
        String instructorName,
        int enrolledStudents,
        LocalDateTime createdAt
) {
}
