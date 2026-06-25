package com.marcoscode.elearning.resource;


import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.lecture.Lecture;
import com.marcoscode.elearning.lecture.LectureRepository;
import com.marcoscode.elearning.resource.dto.ResourceCreateDto;
import com.marcoscode.elearning.resource.dto.ResourceResponseDto;
import com.marcoscode.elearning.resource.dto.ResourceUpdateDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final LectureRepository lectureRepository;

    @PreAuthorize(
            "@securityService.ownsLecture(#lectureId)"
    )
    public ResourceResponseDto createResource(Long lectureId, ResourceCreateDto resourceCreateDto) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(()-> new ResourceNotFoundException("Lecture with Id: " + lectureId + " not found"));

        boolean urlExists = lecture.getResources().stream()
                .anyMatch(r -> r.getFileUrl().equalsIgnoreCase(resourceCreateDto.fileUrl().trim()));

        if (urlExists) {
            throw new IllegalArgumentException("This resource file/URL has already been added to this lecture!");
        }

        Resource resource = resourceMapper.createFromDto(resourceCreateDto);
        lecture.addResource(resource);
        resourceRepository.save(resource);

        return resourceMapper.toResourceResponseDto(resource);
    }

    @Transactional
    @PreAuthorize(
            "hasRole('ADMIN') || @securityService.ownsResource(#resourceId)"
    )

    public ResourceResponseDto updateResource(Long resourceId, @Valid ResourceUpdateDto resourceUpdateDto) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(()-> new ResourceNotFoundException("Resource with Id: " + resourceId + " not found"));

        Long lectureId = resource.getLecture().getId();
        String fileUrl = resourceUpdateDto.fileUrl();

        if(!resource.getFileUrl().equalsIgnoreCase(fileUrl)) {
            boolean urlAlreadyExistsOnLecture = resourceRepository.existsByLectureIdAndFileUrlIgnoreCase(lectureId, fileUrl);

            if (urlAlreadyExistsOnLecture) {
                throw new IllegalArgumentException("A resource with this URL already exists inside this lecture.");
            }
        }

        resourceMapper.updateFromDto(resource, resourceUpdateDto);
        return resourceMapper.toResourceResponseDto(resource);
    }

    @PreAuthorize(
            "hasRole('ADMIN') || @securityService.ownsResource(#resourceId)"
    )
    public void deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(()-> new ResourceNotFoundException("Resource with Id: " + resourceId + " not found"));
        resourceRepository.delete(resource);
    }
}
