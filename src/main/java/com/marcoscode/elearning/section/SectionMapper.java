package com.marcoscode.elearning.section;



import com.marcoscode.elearning.section.dto.SectionCreateDto;
import com.marcoscode.elearning.section.dto.SectionResponseDto;
import com.marcoscode.elearning.section.dto.SectionUpdateDto;
import org.springframework.stereotype.Component;

@Component
public class SectionMapper {

    public Section toSection(SectionCreateDto dto) {
        if (dto == null) {
            return null;
        }

        return Section.builder()
                .title(dto.title())
                .orderIndex(dto.orderIndex())
                .build();
    }

    public  SectionResponseDto toSectionResponseDto(Section section) {
        if (section == null) {
            return null;
        }

        Long courseId = null;
        String courseName = null;

        if (section.getCourse() != null) {
            courseId = section.getCourse().getId();
            courseName = section.getCourse().getTitle();
        }

        return new SectionResponseDto(
                section.getId(),
                section.getTitle(),
                section.getOrderIndex(),
                courseId,
                courseName
        );

    }

    public void updateFromDto(Section existingSection, SectionUpdateDto dto) {
        if (dto == null || existingSection == null) {
            return;
        }

        if (dto.title()  != null && !dto.title().isEmpty()) {
            existingSection.setTitle(dto.title().trim());
        }

        if (dto.orderIndex() != null) {
            existingSection.setOrderIndex(dto.orderIndex());
        }
    }

}
