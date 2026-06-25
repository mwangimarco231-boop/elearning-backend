package com.marcoscode.elearning.instructor;

import com.marcoscode.elearning.instructor.dto.InstructorCreateDto;
import com.marcoscode.elearning.instructor.dto.InstructorDashboardDto;
import com.marcoscode.elearning.instructor.dto.InstructorPublicResponseDto;
import com.marcoscode.elearning.instructor.dto.InstructorUpdateDto;
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
@RequestMapping("/api/v1/instructors")
public class InstructorController {

    private final InstructorService instructorService;

    @GetMapping
    public ResponseEntity<Page<InstructorPublicResponseDto>> getInstructors(
            Pageable pageable
    ) {
        return ResponseEntity
                .ok(instructorService.getInstructors(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstructorPublicResponseDto> getInstructorById(
            @PathVariable Long id
    ) {
        return ResponseEntity
                .ok(instructorService.getInstructorById(id));
    }

    @GetMapping("/my-profile")
    public ResponseEntity<InstructorDashboardDto> getMyProfile(
    ){
        return ResponseEntity.ok(instructorService.getMyProfile());
    }

    @PostMapping
    public ResponseEntity<InstructorPublicResponseDto>registerInstructor(
            @Valid @RequestBody InstructorCreateDto createDto
            ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(instructorService.registerInstructor(createDto));
    }

    @PutMapping("/my-profile")
    public ResponseEntity<InstructorPublicResponseDto> updateMyProfile(
            @Valid @RequestBody InstructorUpdateDto updateDto
    ){
        return ResponseEntity
                .ok(instructorService.updateMyProfile(updateDto));
    }
}
