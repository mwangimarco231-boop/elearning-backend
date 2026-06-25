package com.marcoscode.elearning.instructor;

import org.springframework.data.jpa.repository.JpaRepository;


public interface InstructorRepository
        extends JpaRepository<Instructor, Long> {

    boolean existsByEmail(String email);
}
