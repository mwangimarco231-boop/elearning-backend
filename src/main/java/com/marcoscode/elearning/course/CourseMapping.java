package com.marcoscode.elearning.course;

import com.marcoscode.elearning.course.dto.CourseCreateDto;
import com.marcoscode.elearning.course.dto.CourseResponseDto;
import com.marcoscode.elearning.course.dto.CourseUpdateDto;
import com.marcoscode.elearning.instructor.Instructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CourseMapping {

    public Course createFromDto(CourseCreateDto dto){
        if (dto == null){
            return null;
        }

        Course course = new Course();
        course.setCourseLevel(dto.courseLevel());
        course.setPrice(dto.price());
        course.setTitle(dto.title());

        Instructor instructor = new Instructor();

        instructor.setId(dto.instructorId());
        course.setInstructor(instructor);

        return course;
    }

    public void updateCourseFromDto(Course existingCourse, CourseUpdateDto updateDto){
        if (existingCourse == null || updateDto == null){
            return;
        }

        if (updateDto.title() != null &&
                !Objects.equals(existingCourse.getTitle(), updateDto.title())){
            existingCourse.setTitle(updateDto.title());
        }
        if (updateDto.price() != null &&
                !Objects.equals(existingCourse.getPrice(), updateDto.price())){
            existingCourse.setPrice(updateDto.price());
        }
        if (updateDto.courseLevel() != null &&
                !Objects.equals(existingCourse.getCourseLevel(), updateDto.courseLevel())){
            existingCourse.setCourseLevel(updateDto.courseLevel());
        }
    }



    public CourseResponseDto toCourseResponseDto(Course course){
        if (course == null){
            return null;
        }

        Long instructorId = course.getInstructor().getId() == null ? 0L : course.getInstructor().getId();


        String instructorName = String.format(
                "%s %s",
                course.getInstructor().getFirstName() == null ? "" : course.getInstructor().getFirstName(),
                course.getInstructor().getLastName() == null ? "" : course.getInstructor().getLastName()
        );

        int enrolledStudents = course.getEnrollments().size();

        return new CourseResponseDto(
                course.getId(),
                course.getTitle(),
                course.getPrice(),
                course.getCourseLevel(),
                instructorId,
                instructorName,
                enrolledStudents,
                course.getCreatedAt()
        );
    }
}
