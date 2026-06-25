package com.marcoscode.elearning.resource;

import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.lecture.Lecture;
import com.marcoscode.elearning.lecture.LectureRepository;
import com.marcoscode.elearning.resource.dto.ResourceCreateDto;
import com.marcoscode.elearning.resource.dto.ResourceResponseDto;
import com.marcoscode.elearning.resource.dto.ResourceUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private LectureRepository lectureRepository;

    @InjectMocks
    private ResourceService resourceService;

    @Nested
    @DisplayName("Create Resource Tests")
    class CreateResourceTests {

        @Test
        @DisplayName("Should successfully save resource and add to lecture when URL is unique")
        void shouldCreateResourceSuccessfully() {
            Long lectureId = 1L;

            ResourceCreateDto createDto = new ResourceCreateDto(
                    "Notes",
                    "https://example.com",
                    ResourceType.FILE
            );

            Lecture lecture = Lecture.builder()
                    .title("Introduction")
                    .resources(new ArrayList<>())
                    .build();

            Resource resource = Resource.builder()
                    .title("Notes")
                    .fileUrl("https://example.com")
                    .build();

            ResourceResponseDto expectedResponse = new ResourceResponseDto(
                    10L,
                    "Notes",
                    "https://example.com",
                    ResourceType.FILE
            );

            when(lectureRepository.findById(lectureId))
                    .thenReturn(Optional.of(lecture));

            when(resourceMapper.createFromDto(createDto))
                    .thenReturn(resource);

            when(resourceMapper.toResourceResponseDto(any(Resource.class)))
                    .thenReturn(expectedResponse);

            ResourceResponseDto result =
                    resourceService.createResource(lectureId, createDto);

            assertNotNull(result);
            assertEquals(10L, result.id());
            assertEquals(1, lecture.getResources().size());

            verify(resourceRepository).save(resource);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when target lecture does not exist")
        void shouldThrowExceptionWhenLectureMissing() {
            Long lectureId = 99L;

            ResourceCreateDto createDto = new ResourceCreateDto(
                    "Notes",
                    "https://example.com",
                    ResourceType.FILE
            );

            when(lectureRepository.findById(lectureId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> resourceService.createResource(lectureId, createDto)
            );

            verifyNoInteractions(resourceMapper, resourceRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when duplicate file URL exists in the lecture")
        void shouldThrowExceptionWhenUrlDuplicate() {
            Long lectureId = 1L;

            ResourceCreateDto createDto = new ResourceCreateDto(
                    "Notes",
                    "https://example.com",
                    ResourceType.FILE
            );

            Resource existingResource = Resource.builder()
                    .title("Old Notes")
                    .fileUrl("https://example.com")
                    .build();

            Lecture lecture = Lecture.builder()
                    .title("Introduction")
                    .resources(new ArrayList<>(List.of(existingResource)))
                    .build();

            when(lectureRepository.findById(lectureId))
                    .thenReturn(Optional.of(lecture));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> resourceService.createResource(lectureId, createDto)
            );

            assertEquals(
                    "This resource file/URL has already been added to this lecture!",
                    exception.getMessage()
            );

            verify(resourceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should detect duplicate URLs regardless of case")
        void shouldThrowExceptionWhenUrlDuplicateIgnoringCase() {
            Long lectureId = 1L;

            ResourceCreateDto createDto = new ResourceCreateDto(
                    "Notes",
                    "https://example.com",
                    ResourceType.FILE
            );

            Resource existingResource = Resource.builder()
                    .title("Existing")
                    .fileUrl("HTTPS://EXAMPLE.COM")
                    .build();

            Lecture lecture = Lecture.builder()
                    .resources(new ArrayList<>(List.of(existingResource)))
                    .build();

            when(lectureRepository.findById(lectureId))
                    .thenReturn(Optional.of(lecture));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> resourceService.createResource(lectureId, createDto)
            );

            assertEquals(
                    "This resource file/URL has already been added to this lecture!",
                    exception.getMessage()
            );

            verify(resourceRepository, never()).save(any());
            verify(resourceMapper, never()).createFromDto(any());
        }

        @Test
        @DisplayName("Should detect duplicate URLs after trimming whitespace")
        void shouldThrowExceptionWhenUrlDuplicateAfterTrimmingWhitespace() {
            Long lectureId = 1L;

            ResourceCreateDto createDto = new ResourceCreateDto(
                    "Notes",
                    "https://example.com ",
                    ResourceType.FILE
            );

            Resource existingResource = Resource.builder()
                    .title("Existing")
                    .fileUrl("https://example.com")
                    .build();

            Lecture lecture = Lecture.builder()
                    .resources(new ArrayList<>(List.of(existingResource)))
                    .build();

            when(lectureRepository.findById(lectureId))
                    .thenReturn(Optional.of(lecture));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> resourceService.createResource(lectureId, createDto)
            );

            assertEquals(
                    "This resource file/URL has already been added to this lecture!",
                    exception.getMessage()
            );

            verify(resourceRepository, never()).save(any());
            verify(resourceMapper, never()).createFromDto(any());
        }
    }

    @Nested
    @DisplayName("Update Resource Tests")
    class UpdateResourceTests {

        @Test
        @DisplayName("Should update resource fields successfully when URL is modified to a new unique value")
        void shouldUpdateResourceSuccessfully() {
            Long resourceId = 5L;
            Long lectureId = 1L;

            ResourceUpdateDto updateDto = new ResourceUpdateDto(
                    "Updated Title",
                    "https://newurl.com",
                    ResourceType.FILE
            );

            Lecture lecture = new Lecture();
            lecture.setId(lectureId);

            Resource existingResource = Resource.builder()
                    .id(resourceId)
                    .title("Old Title")
                    .lecture(lecture)
                    .fileUrl("https://oldurl.com")
                    .build();

            ResourceResponseDto expectedResponse = new ResourceResponseDto(
                    resourceId,
                    "Updated Title",
                    "https://newurl.com",
                    ResourceType.FILE
            );

            when(resourceRepository.findById(resourceId))
                    .thenReturn(Optional.of(existingResource));

            when(resourceRepository.existsByLectureIdAndFileUrlIgnoreCase(
                    lectureId,
                    "https://newurl.com"))
                    .thenReturn(false);

            doAnswer(invocation -> {
                Resource target = invocation.getArgument(0);
                ResourceUpdateDto dto = invocation.getArgument(1);

                target.setTitle(dto.title());
                target.setFileUrl(dto.fileUrl());
                target.setResourceType(dto.resourceType());

                return null;
            }).when(resourceMapper)
                    .updateFromDto(any(Resource.class), any(ResourceUpdateDto.class));

            when(resourceMapper.toResourceResponseDto(existingResource))
                    .thenReturn(expectedResponse);

            ResourceResponseDto result =
                    resourceService.updateResource(resourceId, updateDto);

            assertNotNull(result);
            assertEquals("Updated Title", result.title());

            verify(resourceRepository)
                    .existsByLectureIdAndFileUrlIgnoreCase(
                            lectureId,
                            "https://newurl.com"
                    );

            verify(resourceMapper)
                    .updateFromDto(existingResource, updateDto);
        }

        @Test
        @DisplayName("Should skip database check entirely when updated URL is identical to current URL")
        void shouldSkipValidationWhenUrlHasNotChanged() {
            Long resourceId = 5L;
            Long lectureId = 1L;

            ResourceUpdateDto updateDto = new ResourceUpdateDto(
                    "Just changing title",
                    "https://sameurl.com",
                    ResourceType.FILE
            );

            Lecture lecture = new Lecture();
            lecture.setId(lectureId);

            Resource existingResource = Resource.builder()
                    .title("Old Title")
                    .lecture(lecture)
                    .fileUrl("https://sameurl.com")
                    .build();

            ResourceResponseDto expectedResponse = new ResourceResponseDto(
                    resourceId,
                    "Just changing title",
                    "https://sameurl.com",
                    ResourceType.FILE
            );

            when(resourceRepository.findById(resourceId))
                    .thenReturn(Optional.of(existingResource));

            when(resourceMapper.toResourceResponseDto(existingResource))
                    .thenReturn(expectedResponse);

            ResourceResponseDto result =
                    resourceService.updateResource(resourceId, updateDto);

            assertNotNull(result);

            verify(resourceRepository, never())
                    .existsByLectureIdAndFileUrlIgnoreCase(anyLong(), anyString());

            verify(resourceMapper)
                    .updateFromDto(existingResource, updateDto);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when changing to a URL already claimed by another resource in the same lecture")
        void shouldThrowExceptionWhenUrlAlreadyExistsOnLecture() {
            Long resourceId = 5L;
            Long lectureId = 1L;
            String conflictingUrl = "https://already-exists.com";

            ResourceUpdateDto updateDto = new ResourceUpdateDto(
                    "New Title",
                    conflictingUrl,
                    ResourceType.FILE
            );

            Lecture lecture = new Lecture();
            lecture.setId(lectureId);

            Resource existingResource = Resource.builder()
                    .title("Old Title")
                    .fileUrl("https://oldurl.com")
                    .lecture(lecture)
                    .build();

            when(resourceRepository.findById(resourceId))
                    .thenReturn(Optional.of(existingResource));

            when(resourceRepository.existsByLectureIdAndFileUrlIgnoreCase(
                    lectureId,
                    conflictingUrl))
                    .thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> resourceService.updateResource(resourceId, updateDto)
            );

            assertEquals(
                    "A resource with this URL already exists inside this lecture.",
                    exception.getMessage()
            );

            verify(resourceMapper, never())
                    .updateFromDto(any(), any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when update target ID does not exist")
        void shouldThrowExceptionWhenUpdatingMissingResource() {
            Long resourceId = 99L;

            ResourceUpdateDto updateDto = new ResourceUpdateDto(
                    "Title",
                    "https://url.com",
                    ResourceType.FILE
            );

            when(resourceRepository.findById(resourceId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> resourceService.updateResource(resourceId, updateDto)
            );

            verifyNoInteractions(resourceMapper);
        }

        @Test
        @DisplayName("Should update all resource fields correctly")
        void shouldUpdateAllFieldsCorrectly() {
            Long resourceId = 5L;
            Long lectureId = 1L;

            Lecture lecture = new Lecture();
            lecture.setId(lectureId);

            Resource existingResource = Resource.builder()
                    .id(resourceId)
                    .title("Old Title")
                    .fileUrl("https://old.com")
                    .resourceType(ResourceType.FILE)
                    .lecture(lecture)
                    .build();

            ResourceUpdateDto updateDto = new ResourceUpdateDto(
                    "New Title",
                    "https://new.com",
                    ResourceType.FILE
            );

            when(resourceRepository.findById(resourceId))
                    .thenReturn(Optional.of(existingResource));

            when(resourceRepository.existsByLectureIdAndFileUrlIgnoreCase(
                    lectureId,
                    "https://new.com"))
                    .thenReturn(false);

            doAnswer(invocation -> {
                Resource resource = invocation.getArgument(0);
                ResourceUpdateDto dto = invocation.getArgument(1);

                resource.setTitle(dto.title());
                resource.setFileUrl(dto.fileUrl());
                resource.setResourceType(dto.resourceType());

                return null;
            }).when(resourceMapper)
                    .updateFromDto(any(Resource.class), any(ResourceUpdateDto.class));

            when(resourceMapper.toResourceResponseDto(any(Resource.class)))
                    .thenReturn(new ResourceResponseDto(
                            resourceId,
                            "New Title",
                            "https://new.com",
                            ResourceType.FILE
                    ));

            resourceService.updateResource(resourceId, updateDto);

            assertEquals("New Title", existingResource.getTitle());
            assertEquals("https://new.com", existingResource.getFileUrl());
            assertEquals(ResourceType.FILE, existingResource.getResourceType());

            verify(resourceMapper)
                    .updateFromDto(existingResource, updateDto);
        }
    }

    @Nested
    @DisplayName("Delete Resource Tests")
    class DeleteResourceTests {

        @Test
        @DisplayName("Should invoke database deletion routines when resource verification passes")
        void shouldDeleteResourceSuccessfully() {
            Long resourceId = 12L;

            Resource targetResource = Resource.builder()
                    .title("Throwaway resource")
                    .build();

            when(resourceRepository.findById(resourceId))
                    .thenReturn(Optional.of(targetResource));

            assertDoesNotThrow(
                    () -> resourceService.deleteResource(resourceId)
            );

            verify(resourceRepository).delete(targetResource);
            verifyNoInteractions(resourceMapper);
        }

        @Test
        @DisplayName("Should fail deletion sequences immediately when resource target is missing")
        void shouldThrowExceptionWhenDeletingMissingResource() {
            Long resourceId = 99L;

            when(resourceRepository.findById(resourceId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> resourceService.deleteResource(resourceId)
            );

            verify(resourceRepository, never()).delete(any(Resource.class));
        }
    }
}