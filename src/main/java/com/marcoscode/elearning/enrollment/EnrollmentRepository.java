package com.marcoscode.elearning.enrollment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentIdIn(List<Long> studentIds);
    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
