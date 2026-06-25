package com.marcoscode.elearning.course;


import com.marcoscode.elearning.course.dto.CourseCreateDto;
import com.marcoscode.elearning.course.dto.CourseResponseDto;
import com.marcoscode.elearning.course.dto.CourseUpdateDto;
import com.marcoscode.elearning.course.dto.CourseUpdateInstructorIdDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseController {
    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<Page<CourseResponseDto>> getCourses(
            Pageable pageable
    ){
        return ResponseEntity.ok(courseService.getCourses(pageable));
    }

    @GetMapping("/my-courses")
    public ResponseEntity<Page<CourseResponseDto>> myCourses(Pageable pageable){
        return ResponseEntity.ok(courseService.myCourses(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> getCourseById(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PostMapping()
    public ResponseEntity<CourseResponseDto> createCourse(
            @Valid @RequestBody CourseCreateDto courseCreateDto){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(courseService.createCourse(courseCreateDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateDto courseUpdateDto){
        return ResponseEntity.ok(courseService.updateCourse(id, courseUpdateDto));
    }

    @PutMapping("/{id}/instructor")
    public ResponseEntity<CourseResponseDto> updateCourseInstructorId(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateInstructorIdDto courseUpdateInstructorIdDto
    ){
        return ResponseEntity.ok(courseService.updateCourseInstructorId(id, courseUpdateInstructorIdDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id
    ){
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
