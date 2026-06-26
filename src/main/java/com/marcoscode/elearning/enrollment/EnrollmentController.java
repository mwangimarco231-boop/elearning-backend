package com.marcoscode.elearning.enrollment;


import com.marcoscode.elearning.enrollment.dto.EnrollmentCreateDto;
import com.marcoscode.elearning.enrollment.dto.EnrollmentResponseDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/enrollments")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;


    @GetMapping
    public ResponseEntity<Page<EnrollmentResponseDto>> getEnrollments(
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                enrollmentService.getEnrollments(pageable));
    }

    @GetMapping("my-enrollments")
    public ResponseEntity<Page<EnrollmentResponseDto>> getMyEnrollments(
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                enrollmentService.getMyEnrollments(pageable));
        }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDto> getEnrollmentById(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(enrollmentService.getEnrollmentsById(id));
    }


    @PostMapping
    public ResponseEntity<EnrollmentResponseDto> createEnrollment(
            @Valid @RequestBody EnrollmentCreateDto enrollmentCreateDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(enrollmentService.createEnrollment(enrollmentCreateDto));
    }


    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteEnrollment(
            @PathVariable Long id
    ){
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}
