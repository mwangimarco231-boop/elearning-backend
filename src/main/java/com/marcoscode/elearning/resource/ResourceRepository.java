package com.marcoscode.elearning.resource;


import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    boolean existsByLectureIdAndFileUrlIgnoreCase(Long lectureId, String fileUrl);
}
