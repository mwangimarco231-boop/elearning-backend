package com.marcoscode.elearning.section;


import com.marcoscode.elearning.section.dto.SectionCreateDto;
import com.marcoscode.elearning.section.dto.SectionResponseDto;
import com.marcoscode.elearning.section.dto.SectionUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1")
public class SectionController {
    private final SectionService sectionService;

    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<Page<SectionResponseDto>> getSectionByCourseId(
            Pageable pageable,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(sectionService.getSectionByCourseId(courseId, pageable));
    }

    @GetMapping("/sections/{sectionId}")
    public SectionResponseDto getSectionById(
            @PathVariable Long sectionId){
        return sectionService.getSectionById(sectionId);
    }

    @PostMapping("/courses/{courseId}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    public SectionResponseDto createSection(
            @PathVariable Long courseId,
            @Valid @RequestBody SectionCreateDto sectionCreateDto
    ){
        return sectionService.createSection(courseId, sectionCreateDto);
    }

    @PutMapping("/sections/{sectionId}")
    public SectionResponseDto updateSection(
            @PathVariable Long sectionId,
            @Valid @RequestBody SectionUpdateDto sectionUpdateDto
    ){
        return sectionService.updateSection(sectionId, sectionUpdateDto);
    }

    @DeleteMapping("/sections/{sectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSectionById(
            @PathVariable Long sectionId
    ){
        sectionService.deleteSection(sectionId);
    }
}
