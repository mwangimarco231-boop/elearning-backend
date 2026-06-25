package com.marcoscode.elearning.section;

import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.course.CourseRepository;
import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.section.dto.SectionCreateDto;
import com.marcoscode.elearning.section.dto.SectionResponseDto;
import com.marcoscode.elearning.section.dto.SectionUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SectionMapper sectionMapper;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private SectionService sectionService;

    @Nested
    @DisplayName("Get Sections Tests")
    class GetSectionsTests {

        @Test
        @DisplayName("Should return paginated sections when course exists")
        void getSectionByCourseId_Success() {
            Long courseId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Section mockSection = Section.builder().id(10L).title("Introduction").orderIndex(1).build();
            Page<Section> mockPage = new PageImpl<>(List.of(mockSection), pageable, 1);

            SectionResponseDto mockDto = new SectionResponseDto(
                    10L,
                    "Introduction",
                    1,
                    2L,
                    "mechanical"
                    );

            when(courseRepository.existsById(courseId)).thenReturn(true);
            when(sectionRepository.findByCourseId(courseId, pageable)).thenReturn(mockPage);
            when(sectionMapper.toSectionResponseDto(mockSection)).thenReturn(mockDto);

            Page<SectionResponseDto> result = sectionService.getSectionByCourseId(courseId, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Introduction", result.getContent().getFirst().title());
            verify(courseRepository).existsById(courseId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when course does not exist")
        void getSectionByCourseId_CourseNotFound() {
            Long courseId = 99L;
            Pageable pageable = PageRequest.of(0, 10);

            when(courseRepository.existsById(courseId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> sectionService.getSectionByCourseId(courseId, pageable));
            verifyNoInteractions(sectionRepository);
        }

        @Test
        @DisplayName("Should return section DTO when section ID exists")
        void getSectionById_Success() {
            Long sectionId = 10L;
            Section mockSection = Section.builder().id(sectionId).title("Advanced Routing").build();

            SectionResponseDto mockDto = new SectionResponseDto(
                    sectionId,
                    "Advanced Routing",
                    2,
                    2L,
                    "mechanical"
                    );

            when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(mockSection));
            when(sectionMapper.toSectionResponseDto(mockSection)).thenReturn(mockDto);

            SectionResponseDto result = sectionService.getSectionById(sectionId);

            assertNotNull(result);
            assertEquals("Advanced Routing", result.title());
        }
    }

    @Nested
    @DisplayName("Create Section Tests")
    class CreateSectionTests {

        @Test
        @DisplayName("Should create section successfully when parameters are unique")
        void createSection_Success() {
            Long courseId = 1L;
            SectionCreateDto createDto = new SectionCreateDto(
                    "Getting Started",
                    3);

            Course mockCourse = Course.builder().id(courseId).sections(new ArrayList<>()).build();
            Section mockSection = Section.builder().title("Getting Started").orderIndex(1).build();

            SectionResponseDto mockDto = new SectionResponseDto(
                    10L,
                    "Getting Started",
                    1,
                    4L,
                    "mechanical"
            );

            when(sectionRepository.existsByTitleAndCourseId("Getting Started", courseId)).thenReturn(false);
            when(sectionRepository.existsByOrderIndexAndCourseId(3, courseId)).thenReturn(false);
            when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
            when(sectionMapper.toSection(createDto)).thenReturn(mockSection);
            when(sectionRepository.save(mockSection)).thenReturn(mockSection);
            when(sectionMapper.toSectionResponseDto(mockSection)).thenReturn(mockDto);

            SectionResponseDto result = sectionService.createSection(courseId, createDto);

            assertNotNull(result);
            assertEquals("Getting Started", result.title());
            verify(sectionRepository).save(mockSection);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when title unique check fails")
        void createSection_DuplicateTitle_ThrowsException() {
            Long courseId = 1L;
            SectionCreateDto createDto = new SectionCreateDto(
                    "Duplicate Title",
                    2
            );

            when(sectionRepository.existsByTitleAndCourseId("Duplicate Title", courseId)).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> sectionService.createSection(courseId, createDto));
            verify(sectionRepository, never()).existsByOrderIndexAndCourseId(anyInt(), anyLong());
        }

        @Test
        @DisplayName("Should throw IllegalStateException when ordering index conflict occurs")
        void createSection_DuplicateOrderIndex_ThrowsException() {
            Long courseId = 1L;
            SectionCreateDto createDto = new SectionCreateDto(
                    "New Section",
                    2
            );

            when(sectionRepository.existsByTitleAndCourseId("New Section", courseId)).thenReturn(false);
            when(sectionRepository.existsByOrderIndexAndCourseId(2, courseId)).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> sectionService.createSection(courseId, createDto));
        }
    }

    @Nested
    @DisplayName("Update & Delete Section Tests")
    class UpdateDeleteSectionTests {

        @Test
        @DisplayName("Should modify section details when unique validation steps pass")
        void updateSection_Success() {
            Long sectionId = 10L;
            SectionUpdateDto updateDto = new SectionUpdateDto(
                    "Modified Title",
                    3
                    );

            Course mockCourse = Course.builder().id(1L).build();
            Section currentSection = Section.builder().id(sectionId).title("Old Title").orderIndex(2).course(mockCourse).build();

            SectionResponseDto expectedDto = new SectionResponseDto(
                    sectionId,
                    "Modified Title",
                    3,
                    1L,
                    "mechanical"
                    );

            when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(currentSection));
            // Lenient mocks to handle either your original service checks or fixed parameters smoothly
            lenient().when(sectionRepository.existsByTitleAndCourseId(anyString(), anyLong())).thenReturn(false);
            lenient().when(sectionRepository.existsByOrderIndexAndCourseId(anyInt(), anyLong())).thenReturn(false);
            when(sectionMapper.toSectionResponseDto(currentSection)).thenReturn(expectedDto);

            SectionResponseDto result = sectionService.updateSection(sectionId, updateDto);

            assertNotNull(result);
            assertEquals("Modified Title", result.title());
            verify(sectionMapper).updateFromDto(currentSection, updateDto);
        }

        @Test
        @DisplayName("Should physically delete record when database entity ID is matched")
        void deleteSection_Success() {
            Long sectionId = 10L;
            Section currentSection = Section.builder().id(sectionId).build();

            when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(currentSection));

            sectionService.deleteSection(sectionId);

            verify(sectionRepository, times(1)).delete(currentSection);
        }
    }
}
