package com.marcoscode.elearning.lecture;

import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.lecture.dto.LectureCreateDto;
import com.marcoscode.elearning.lecture.dto.LectureResponseDto;
import com.marcoscode.elearning.lecture.dto.LectureUpdateDto;
import com.marcoscode.elearning.section.Section;
import com.marcoscode.elearning.section.SectionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureMapper lectureMapper;
    private final SectionRepository sectionRepository;


    public Page<LectureResponseDto> getLecturesBySectionId(Long sectionId, Pageable pageable) {

        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section with id " + sectionId + " not found");
        }

        return lectureRepository.findBySectionId(sectionId, pageable)
                .map(lectureMapper::toResponseDto);
    }

    public LectureResponseDto getLecturesById(Long id) {
        return lectureRepository.findById(id)
                .map(lectureMapper::toResponseDto)
                .orElseThrow(()-> new ResourceNotFoundException("Lecture with id: " + id + " does not exist"));
    }

    @Transactional
    @PreAuthorize(
            "@securityService.ownSection(#sectionId)"
    )
    public LectureResponseDto createLecture(Long sectionId, @Valid LectureCreateDto createDto) {

        if (lectureRepository.existsByTitleAndDurationSecondsAndSectionId(
                createDto.title(),
                createDto.durationSeconds(),
                sectionId)
        ){
            throw new IllegalStateException("A lecture with title and duration already exists in the section");
        }

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(()-> new ResourceNotFoundException("Section with id: " + sectionId + " does not exist"));

        Lecture lecture = lectureMapper.createFromDto(createDto);
        section.addLecture(lecture);
        lectureRepository.save(lecture);
        return lectureMapper.toResponseDto(lecture);
    }

    @Transactional
    @PreAuthorize(
            "hasRole('ADMIN') || @securityService.ownsLecture(#lectureId)"
    )
    public LectureResponseDto updateLecture(Long lectureId, @Valid LectureUpdateDto updateDto) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(()-> new ResourceNotFoundException("Lecture with id: " + lectureId + " does not exist"));

        Long sectionId = lecture.getSection().getId();

        if (lectureRepository.existsByTitleAndDurationSecondsAndSectionIdAndIdNot(
                updateDto.title(),
                updateDto.durationSeconds(),
                sectionId,
                lectureId)
        ){
            throw new IllegalStateException("A lecture with title and duration already exists in the section");
        }

        lectureMapper.updateFromDto(lecture, updateDto);
        return lectureMapper.toResponseDto(lecture);
    }

    @PreAuthorize(
            "hasRole('ADMIN') || @securityService.ownsLecture(#lectureId)"
    )
    public void deleteLecture(Long lectureId) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(()-> new ResourceNotFoundException("Lecture with id: " + lectureId + " does not exist"));
        lectureRepository.delete(lecture);
    }
}
