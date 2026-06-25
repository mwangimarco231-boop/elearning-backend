package com.marcoscode.elearning.lecture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    Page<Lecture> findBySectionId(Long sectionId, Pageable pageable);

    boolean existsByTitleAndDurationSecondsAndSectionIdAndIdNot(
            String title,
            Integer durationSeconds,
            Long sectionId,
            Long id
    );


    boolean existsByTitleAndDurationSecondsAndSectionId(
            String title,
            Integer durationSeconds,
            Long sectionId
    );
}
