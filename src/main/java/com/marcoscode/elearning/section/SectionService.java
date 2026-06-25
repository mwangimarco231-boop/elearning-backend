package com.marcoscode.elearning.section;

import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.course.CourseRepository;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.section.dto.SectionCreateDto;
import com.marcoscode.elearning.section.dto.SectionResponseDto;
import com.marcoscode.elearning.section.dto.SectionUpdateDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;
    private final CourseRepository courseRepository;

    public Page<SectionResponseDto> getSectionByCourseId(Long courseId, Pageable pageable) {

        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course with id: " + courseId + " not found");
        }

        Page<Section> sections = sectionRepository.findByCourseId(courseId, pageable);

        return sections
                .map(sectionMapper::toSectionResponseDto);
    }

    public SectionResponseDto getSectionById(Long sectionId) {
       return sectionRepository.findById(sectionId)
               .map(sectionMapper::toSectionResponseDto)
               .orElseThrow(()-> new ResourceNotFoundException("Section with id: " + sectionId + " not found"));
    }

    @Transactional
    @PreAuthorize(
            "@securityService.ownsCourse(#courseId)"
    )
    public SectionResponseDto createSection(Long courseId, SectionCreateDto sectionCreateDto) {

        if(sectionRepository.existsByTitleAndCourseId(sectionCreateDto.title(), courseId)) {
            throw new IllegalStateException
                    ("A section named '" + sectionCreateDto.title() + "' already exists in this course");
        }

        if (sectionRepository.existsByOrderIndexAndCourseId(sectionCreateDto.orderIndex(), courseId)) {
            throw new IllegalStateException
                    ("A section with ordering index " + sectionCreateDto.orderIndex() + " already exists in this course");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(()-> new ResourceNotFoundException("course with id: " + courseId + " does not exists"));

        Section section = sectionMapper.toSection(sectionCreateDto);
        course.addSection(section);
        Section savedSection = sectionRepository.save(section);

        return sectionMapper.toSectionResponseDto(savedSection);
    }

    @Transactional
    @PreAuthorize(
            "hasRole('ADMIN') || @securityService.ownSection(#sectionId)"
    )
    public SectionResponseDto updateSection(Long sectionId, SectionUpdateDto sectionUpdateDto) {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(()-> new ResourceNotFoundException("Section with id: " + sectionId + " not found"));

        Long courseId = section.getCourse().getId();


        if(sectionRepository.existsByTitleAndCourseId(sectionUpdateDto.title(), courseId)) {
            throw new IllegalStateException
                    ("A section named '" + sectionUpdateDto.title() + "' already exists in this course");
        }

        if (sectionRepository.existsByOrderIndexAndCourseId(sectionUpdateDto.orderIndex(), courseId)) {
            throw new IllegalStateException
                    ("A section with ordering index " + sectionUpdateDto.orderIndex() + " already exists in this course");
        }

        sectionMapper.updateFromDto(section, sectionUpdateDto);

        return sectionMapper.toSectionResponseDto(section);
    }

    @Transactional
    @PreAuthorize(
            "hasRole('ADMIN') || @securityService.ownSection(#sectionId)"
    )
    public void deleteSection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(()-> new ResourceNotFoundException("Section with id: " + sectionId + " not found"));

        sectionRepository.delete(section);
    }
}
