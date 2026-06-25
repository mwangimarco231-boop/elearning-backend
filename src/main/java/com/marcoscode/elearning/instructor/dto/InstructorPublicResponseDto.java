package com.marcoscode.elearning.instructor.dto;

import java.util.List;

public record InstructorPublicResponseDto(
        Long instructorId,
        String instructorName,
        String bio,
        int totalCoursesTaught,
        List<InstructorPublicCoursesSummaryDto> instructorCourses
) {
}
