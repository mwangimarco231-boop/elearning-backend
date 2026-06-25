package com.marcoscode.elearning.lecture;

import com.marcoscode.elearning.lecture.dto.LectureCreateDto;
import com.marcoscode.elearning.lecture.dto.LectureResponseDto;
import com.marcoscode.elearning.lecture.dto.LectureUpdateDto;
import com.marcoscode.elearning.resource.Resource;
import com.marcoscode.elearning.resource.ResourceType;
import com.marcoscode.elearning.resource.dto.ResourceResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LectureMapper {


    public LectureResponseDto toResponseDto(Lecture lecture) {
        if (lecture == null) {
            return null;
        }

        Long sectionId = null;
        String sectionName = null;

        if (lecture.getSection() != null) {
            sectionId = lecture.getSection().getId();
            sectionName = lecture.getSection().getTitle();
        }

        List<ResourceResponseDto> resourceResponseDtoList = new ArrayList<>();
        if (lecture.getResources() != null) {
            resourceResponseDtoList = lecture.getResources()
                    .stream()
                    .map(this::resourceResponseDto)
                    .collect(Collectors.toList());
        }

        return new LectureResponseDto(
                lecture.getId(),
                lecture.getTitle(),
                lecture.getDurationSeconds(),
                sectionId,
                sectionName,
                resourceResponseDtoList
        );
    }

    private ResourceResponseDto resourceResponseDto(Resource resource) {
        if (resource == null) {
            return null;
        }

        return  new ResourceResponseDto(
                resource.getId(),
                resource.getTitle(),
                resource.getFileUrl(),
                resource.getResourceType());
    }

    public Lecture createFromDto(LectureCreateDto dto) {
        if (dto == null) {
            return null;
        }

        return Lecture.builder()
                .title(dto.title())
                .durationSeconds(dto.durationSeconds())
                .build();
    }

    public void updateFromDto(Lecture existingLecture, LectureUpdateDto dto) {
        if (dto == null || existingLecture == null) {
            return;
        }

        if (dto.title() != null) {
            existingLecture.setTitle(dto.title());
        }

        if (dto.durationSeconds() != null) {
            existingLecture.setDurationSeconds(dto.durationSeconds());
        }

    }
}