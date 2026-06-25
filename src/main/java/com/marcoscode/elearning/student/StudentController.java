package com.marcoscode.elearning.student;

import com.marcoscode.elearning.student.dto.StudentCreateDto;
import com.marcoscode.elearning.student.dto.StudentResponseDto;
import com.marcoscode.elearning.student.dto.StudentUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<Page<StudentResponseDto>> getStudents(
            Pageable pageable
    ) {
        return ResponseEntity.ok(studentService.getStudents(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("my-profile")
    public ResponseEntity<StudentResponseDto> getMyOwnProfile(
    ){
        return ResponseEntity.ok(studentService.getMyOwnProfile());
    }

    @PostMapping
    public ResponseEntity<StudentResponseDto> createStudent(
            @Valid @RequestBody StudentCreateDto createDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(studentService.createStudent(createDto));
    }

    @PutMapping("/my-profile")
    public ResponseEntity<StudentResponseDto> updateMyProfile(
            @Valid @RequestBody StudentUpdateDto updateDto
    ) {
        return ResponseEntity.ok(studentService.updateMyProfile(updateDto));
    }
}
