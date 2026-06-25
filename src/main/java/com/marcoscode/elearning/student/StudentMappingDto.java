package com.marcoscode.elearning.student;


import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.student.dto.StudentCourseSummaryDto;
import com.marcoscode.elearning.student.dto.StudentCreateDto;
import com.marcoscode.elearning.student.dto.StudentResponseDto;
import com.marcoscode.elearning.student.dto.StudentUpdateDto;
import com.marcoscode.elearning.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StudentMappingDto {

    private final PasswordEncoder passwordEncoder;

    public StudentResponseDto toStudentResponseDto(Student student, List<Course> courses) {
        if (student == null) {
            return null;
        }

        List<StudentCourseSummaryDto> studentCourses = courses.stream()
                .map(this::toCourseSummaryDtos)
                .toList();

        int enrolledCourses = studentCourses.size();

        String fullName = String.format(
                "%s %s",
                student.getFirstName() == null ? "" : student.getFirstName(),
                student.getLastName() == null ? "" : student.getLastName()).trim();

        return new StudentResponseDto(
                student.getId(),
                fullName,
                student.getEmail(),
                student.getCreatedAt(),
                enrolledCourses,
                studentCourses
        );
    }

    public StudentCourseSummaryDto toCourseSummaryDtos(Course course){
        if (course == null) {
            return null;
        }

        String instructorName = "";
        if (course.getInstructor() != null) {
            instructorName = String.format(
                    "%s %s",
                    course.getInstructor().getFirstName() == null ? "" : course.getInstructor().getFirstName(),
                    course.getInstructor().getLastName() == null ? "" : course.getInstructor().getLastName()
            );
        }

        return new StudentCourseSummaryDto(
                course.getId(),
                course.getTitle(),
                course.getCourseLevel(),
                instructorName
        );
    }

    public Student createFromDto(StudentCreateDto dto) {
        if (dto == null) {
            return null;
        }

        return Student.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.STUDENT)
                .email(dto.email())
                .build();
    }

    public void updateFromDto(Student student, StudentUpdateDto dto) {
        if (dto == null || student == null) {
            return;
        }

        if (dto.firstName() != null
                && !Objects.equals(dto.firstName(), student.getFirstName())) {
            student.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null
                && !Objects.equals(dto.lastName(), student.getLastName())) {
            student.setLastName(dto.lastName());
        }

        if (dto.email() != null
                && !Objects.equals(dto.email(), student.getEmail())) {
            student.setEmail(dto.email());
        }
    }
}
