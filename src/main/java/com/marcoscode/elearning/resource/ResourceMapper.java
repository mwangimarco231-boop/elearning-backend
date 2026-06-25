package com.marcoscode.elearning.resource;


import com.marcoscode.elearning.lecture.Lecture;
import com.marcoscode.elearning.resource.dto.ResourceCreateDto;
import com.marcoscode.elearning.resource.dto.ResourceResponseDto;
import com.marcoscode.elearning.resource.dto.ResourceUpdateDto;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public Resource createFromDto (ResourceCreateDto dto) {
        if (dto == null) {
            return null;
        }

        return Resource.builder()
                .title(dto.title())
                .resourceType(dto.resourceType())
                .fileUrl(dto.fileUrl())
                .build();
    }

    public void updateFromDto (Resource existingResource, ResourceUpdateDto dto) {
        if (dto == null || existingResource == null) {
            return;
        }

        if (dto.title() != null) {
            existingResource.setTitle(dto.title());
        }

        if (dto.resourceType() != null) {
            existingResource.setResourceType(dto.resourceType());
        }

        if (dto.fileUrl() != null) {
            existingResource.setFileUrl(dto.fileUrl());
        }
    }

    public ResourceResponseDto toResourceResponseDto(Resource resource) {
        if (resource == null) {
            return null;
        }

        return  new ResourceResponseDto(
                resource.getId(),
                resource.getTitle(),
                resource.getFileUrl(),
                resource.getResourceType());
    }
}
