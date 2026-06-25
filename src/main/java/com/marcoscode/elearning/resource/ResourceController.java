package com.marcoscode.elearning.resource;


import com.marcoscode.elearning.resource.dto.ResourceCreateDto;
import com.marcoscode.elearning.resource.dto.ResourceResponseDto;
import com.marcoscode.elearning.resource.dto.ResourceUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @PostMapping("/lectures/{lectureId}/resource")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponseDto createResource(
            @PathVariable Long lectureId,
            @Valid @RequestBody ResourceCreateDto resourceCreateDto
    ){
        return resourceService.createResource(lectureId, resourceCreateDto);
    }

    @PutMapping("/resources/{resourceId}")
    @ResponseStatus(HttpStatus.OK)
    public ResourceResponseDto updateResource(
            @PathVariable Long resourceId,
            @Valid @RequestBody ResourceUpdateDto resourceUpdateDto
    ){
        return resourceService.updateResource(resourceId, resourceUpdateDto);
    }

    @DeleteMapping("/resources/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(
            @PathVariable Long resourceId
    ){
        resourceService.deleteResource(resourceId);
    }
}