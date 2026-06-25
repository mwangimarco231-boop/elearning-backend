package com.marcoscode.elearning.lecture;

import com.marcoscode.elearning.exception.ResourceNotFoundException;
import com.marcoscode.elearning.lecture.dto.LectureCreateDto;
import com.marcoscode.elearning.lecture.dto.LectureResponseDto;
import com.marcoscode.elearning.lecture.dto.LectureUpdateDto;
import com.marcoscode.elearning.section.Section;
import com.marcoscode.elearning.section.SectionRepository;
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
class LectureServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LectureMapper lectureMapper;

    @Mock
    private SectionRepository sectionRepository;

    @InjectMocks
    private LectureService lectureService;

    @Nested
    @DisplayName("Get Lectures by Section Tests")
    class GetLecturesBySectionTests {

        @Test
        @DisplayName("Should return paginated lectures when section exists")
        void getLecturesBySection_Success() {
            Long sectionId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Lecture lecture = Lecture.builder().id(10L).title("Variables").durationSeconds(300).build();
            Page<Lecture> mockPage = new PageImpl<>(List.of(lecture), pageable, 1);
            LectureResponseDto responseDto = new LectureResponseDto(10L, "Variables", 300, sectionId, "Basics", new ArrayList<>());

            when(sectionRepository.existsById(sectionId)).thenReturn(true);
            when(lectureRepository.findBySectionId(sectionId, pageable)).thenReturn(mockPage);
            when(lectureMapper.toResponseDto(lecture)).thenReturn(responseDto);

            Page<LectureResponseDto> result = lectureService.getLecturesBySectionId(sectionId, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Variables", result.getContent().getFirst().title());
            verify(sectionRepository).existsById(sectionId);
            verify(lectureRepository).findBySectionId(sectionId, pageable);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when section is missing")
        void getLecturesBySection_SectionNotFound() {
            Long sectionId = 99L;
            Pageable pageable = PageRequest.of(0, 10);

            when(sectionRepository.existsById(sectionId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> lectureService.getLecturesBySectionId(sectionId, pageable));
            verifyNoInteractions(lectureRepository);
        }
    }

    @Nested
    @DisplayName("Get Lecture By ID Tests")
    class GetLectureByIdTests {

        @Test
        @DisplayName("Should return lecture DTO when ID exists")
        void getLectureById_Success() {
            Long id = 10L;
            Lecture lecture = Lecture.builder().id(id).title("Variables").durationSeconds(300).build();

            LectureResponseDto responseDto = new LectureResponseDto(id, "Variables", 300, 1L, "Basics", new ArrayList<>());

            when(lectureRepository.findById(id)).thenReturn(Optional.of(lecture));
            when(lectureMapper.toResponseDto(lecture)).thenReturn(responseDto);

            LectureResponseDto result = lectureService.getLecturesById(id);

            assertNotNull(result);
            assertEquals("Variables", result.title());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when lecture ID is missing")
        void getLectureById_NotFound() {
            Long id = 99L;
            when(lectureRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> lectureService.getLecturesById(id));

            verifyNoInteractions(lectureRepository, lectureMapper);
        }
    }

    @Nested
    @DisplayName("Create Lecture Tests")
    class CreateLectureTests {

        @Test
        @DisplayName("Should create lecture successfully when input is unique")
        void createLecture_Success() {

            Long sectionId = 1L;
            LectureCreateDto createDto = new LectureCreateDto(
                    "Loops",
                    400);

            Section section = Section.builder().id(sectionId).title("Basics").lectures(new ArrayList<>()).build();

            Lecture lecture = Lecture.builder().title("Loops").durationSeconds(400).build();

            LectureResponseDto responseDto = new LectureResponseDto(11L, "Loops", 400, sectionId, "Basics", new ArrayList<>());

            when(lectureRepository.existsByTitleAndDurationSecondsAndSectionId("Loops", 400, sectionId)).thenReturn(false);
            when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
            when(lectureMapper.createFromDto(createDto)).thenReturn(lecture);
            when(lectureRepository.save(lecture)).thenReturn(lecture);
            when(lectureMapper.toResponseDto(lecture)).thenReturn(responseDto);

            LectureResponseDto result = lectureService.createLecture(sectionId, createDto);

            assertNotNull(result);
            assertEquals("Loops", result.title());

            verify(lectureRepository, times(1)).existsByTitleAndDurationSecondsAndSectionId("Loops", 400, sectionId);
            verify(sectionRepository, times(1)).existsById(sectionId);
            verify(lectureMapper, times(1)).createFromDto(createDto);
            verify(lectureRepository, times(1)).save(lecture);
            verify(lectureMapper, times(1)).toResponseDto(lecture);

        }

        @Test
        @DisplayName("Should throw IllegalStateException when creating a duplicate combination")
        void createLecture_DuplicateEntry() {
            Long sectionId = 1L;
            LectureCreateDto createDto = new LectureCreateDto("Loops", 400);

            when(lectureRepository.existsByTitleAndDurationSecondsAndSectionId("Loops", 400, sectionId)).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> lectureService.createLecture(sectionId, createDto));

            verify(sectionRepository, never()).findById(anyLong());
            verifyNoInteractions(lectureRepository, lectureMapper);

        }
    }

    @Nested
    @DisplayName("Update Lecture Tests")
    class UpdateLectureTests {

        @Test
        @DisplayName("Should update lecture fields cleanly without self-collision blocks")
        void updateLecture_Success() {
            Long lectureId = 10L;
            Long sectionId = 1L;
            LectureUpdateDto updateDto = new LectureUpdateDto("Variables Edited", 350);

            Section section = Section.builder().id(sectionId).build();

            Lecture lecture = Lecture.builder().id(lectureId).title("Variables").durationSeconds(300).section(section).build();

            LectureResponseDto responseDto = new LectureResponseDto(lectureId, "Variables Edited", 350, sectionId, "Basics", new ArrayList<>());

            when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
            when(lectureRepository.existsByTitleAndDurationSecondsAndSectionIdAndIdNot("Variables Edited", 350, sectionId, lectureId)).thenReturn(false);
            when(lectureMapper.toResponseDto(lecture)).thenReturn(responseDto);

            LectureResponseDto result = lectureService.updateLecture(lectureId, updateDto);

            assertNotNull(result);
            assertEquals("Variables Edited", result.title());
            verify(lectureMapper).updateFromDto(lecture, updateDto);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when updating to match another lecture")
        void updateLecture_DuplicateConflict() {
            Long lectureId = 10L;
            Long sectionId = 1L;
            LectureUpdateDto updateDto = new LectureUpdateDto("Existing Title", 500);
            Section section = Section.builder().id(sectionId).build();
            Lecture lecture = Lecture.builder().id(lectureId).section(section).build();

            when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
            when(lectureRepository.existsByTitleAndDurationSecondsAndSectionIdAndIdNot("Existing Title", 500, sectionId, lectureId)).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> lectureService.updateLecture(lectureId, updateDto));

            verifyNoInteractions(lectureMapper);
        }
    }

    @Nested
    @DisplayName("Delete Lecture Tests")
    class DeleteLectureTests {

        @Test
        @DisplayName("Should execute deletion when resource ID exists")
        void deleteLecture_Success() {
            Long id = 10L;
            Lecture lecture = Lecture.builder().id(id).build();

            when(lectureRepository.findById(id)).thenReturn(Optional.of(lecture));

            lectureService.deleteLecture(id);

            verify(lectureRepository, times(1)).delete(lecture);
            verifyNoInteractions(lectureMapper);
        }
    }
}
