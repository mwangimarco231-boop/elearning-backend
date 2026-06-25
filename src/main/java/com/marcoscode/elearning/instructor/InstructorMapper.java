package com.marcoscode.elearning.instructor;

import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.instructor.dto.*;
import com.marcoscode.elearning.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class InstructorMapper {

    private final PasswordEncoder passwordEncoder;

    public InstructorDashboardDto toInstructorResponseDto(Instructor instructor, List<Course> courses) {
        if (instructor == null) {
            return null;
        }

        List<InstructorDashboardCourseSummaryDto> instructorCourses = courses.stream()
                .map(this::toInstructorCourseSummaryDto)
                .toList();

        int totalCourses = instructorCourses.size();

        long totalStudents = instructorCourses.stream()
                .mapToLong(InstructorDashboardCourseSummaryDto::studentCount)
                .sum();

        double totalRevenue = instructorCourses.stream()
                .mapToDouble(c -> c.price() * c.studentCount())
                .sum();

        String instructorName = String.format(
                "%s %s",
                instructor.getFirstName() == null ? "" : instructor.getFirstName(),
                instructor.getLastName() == null ? "" : instructor.getLastName()
        ).trim();

        return new InstructorDashboardDto(
                instructor.getId(),
                instructorName,
                instructor.getEmail(),
                instructor.getBio(),
                instructor.getCreatedAt(),
                totalCourses,
                (int) totalStudents,
                totalRevenue,
                instructorCourses
        );
    }

    public InstructorDashboardCourseSummaryDto toInstructorCourseSummaryDto(Course course ) {
        return new InstructorDashboardCourseSummaryDto(
                course.getId(),
                course.getTitle(),
                course.getPrice(),
                course.getCourseLevel(),
                course.getEnrollments()  != null ? (long) course.getEnrollments().size() : 0L,
                course.getCreatedAt()
        );
    }

    public InstructorPublicResponseDto toInstructorPublicResponseDto(Instructor instructor, List<Course> courses) {
        List<InstructorPublicCoursesSummaryDto> coursesSummary = courses
                .stream()
                .map(this::toInstructorPublicCoursesSummaryDto)
                .toList();

        int totalCourses = coursesSummary.size();

        String instructorName = String.format(
                "%s %s",
                instructor.getFirstName() == null ? "" : instructor.getFirstName(),
                instructor.getLastName() == null ? "" : instructor.getLastName()
        ).trim();


        return new InstructorPublicResponseDto(
                instructor.getId(),
                instructorName,
                instructor.getBio(),
                totalCourses,
                coursesSummary
        );
    }

    public InstructorPublicCoursesSummaryDto  toInstructorPublicCoursesSummaryDto(Course course) {
        return new InstructorPublicCoursesSummaryDto(
                course.getId(),
                course.getTitle(),
                course.getPrice(),
                course.getCourseLevel()
        );
    }

    public Instructor createFromDto(InstructorCreateDto createDto) {
        if (createDto == null) {
            return null;
        }

        return Instructor.builder()
                .firstName(createDto.firstName())
                .lastName(createDto.lastName())
                .email(createDto.email())
                .role(Role.INSTRUCTOR)
                .password(passwordEncoder.encode(createDto.password()))
                .bio(createDto.bio())
                .build();
    }

    public void updateFromDto(Instructor instructor, InstructorUpdateDto updateDto) {

        if (updateDto.firstName() != null &&
                !Objects.equals(updateDto.firstName().trim(), instructor.getFirstName())) {
            instructor.setFirstName(updateDto.firstName());
        }

        if (updateDto.lastName() != null
                && !Objects.equals(updateDto.lastName().trim(), instructor.getLastName())) {
            instructor.setLastName(updateDto.lastName());
        }

        if (updateDto.email() != null
                && !Objects.equals(updateDto.email().trim(), instructor.getEmail())) {
            instructor.setEmail(updateDto.email().trim());
        }

        if (updateDto.bio() != null && !Objects.equals(updateDto.bio().trim(), instructor.getBio())) {
            instructor.setBio(updateDto.bio());
        }
    }
}
