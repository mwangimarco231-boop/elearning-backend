package com.marcoscode.elearning.lecture;


import com.marcoscode.elearning.lecture.dto.LectureCreateDto;
import com.marcoscode.elearning.lecture.dto.LectureResponseDto;
import com.marcoscode.elearning.lecture.dto.LectureUpdateDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class LectureController {

    private final LectureService lectureService;


    @GetMapping("/sections/{sectionId}/lectures")
    public Page<LectureResponseDto> getLecturesBySectionId(
            Pageable pageable,
            @PathVariable Long sectionId
    ) {
        return lectureService.getLecturesBySectionId(sectionId, pageable);
    }

    @GetMapping("/lectures/{id}")
    public LectureResponseDto getLecturesById(
            @PathVariable Long id
    ) {
        return lectureService.getLecturesById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/sections/{sectionId}/lectures")
    public LectureResponseDto createLecture(
            @PathVariable Long sectionId,
            @Valid @RequestBody LectureCreateDto createDto
    ){
        return lectureService.createLecture(sectionId, createDto);
    }

    @PutMapping("/lectures/{lectureId}")
    @SecurityRequirement(name = "bearerAuth")
    public LectureResponseDto updateLecture(
            @PathVariable Long lectureId,
            @Valid @RequestBody LectureUpdateDto updateDto
    ){
        return lectureService.updateLecture(lectureId, updateDto);
    }


    @DeleteMapping("/lectures/{lectureId}")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLecture(
            @PathVariable Long lectureId
    ){
        lectureService.deleteLecture(lectureId);
    }
}
