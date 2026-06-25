package com.marcoscode.elearning.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByInstructorId(Long userId);
    List<Course> findByInstructorIdIn(List<Long> instructorIds);

    boolean existsByTitleAndInstructorIdAndCourseLevel(String title, Long instructorId,  Level courseLevel);

    Page<Course> findByInstructorId(Long id, Pageable pageable);

}
