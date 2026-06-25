package com.marcoscode.elearning.student.dto;

import com.marcoscode.elearning.course.Level;

public record StudentCourseSummaryDto(
        Long courseId,
        String title,
        Level courseLevel,
        String instructor
        //double progress
) {
}
