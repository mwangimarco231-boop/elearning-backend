package com.marcoscode.elearning.section;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {
    boolean existsByTitleAndCourseId(String title, Long courseId);

    boolean existsByOrderIndexAndCourseId(Integer orderIndex, Long courseId);

    Page<Section> findByCourseId(Long curseId, Pageable pageable);
}
