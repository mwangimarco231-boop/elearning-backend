package com.marcoscode.elearning.instructor.dto;



import java.time.LocalDateTime;
import java.util.List;

public record InstructorDashboardDto(
        Long instructorId,
        String instructorName,
        String email,
        String bio,
        LocalDateTime createdAt,

        int totalCoursesTaught,
        int totalActiveStudents,
        Double totalRevenueGenerated,

        List<InstructorDashboardCourseSummaryDto> instructorCourses

) {
}
