package com.marcoscode.elearning.enrollment;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.marcoscode.elearning.common.domain.BaseEntity;
import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.student.Student;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

@Entity()
@EntityListeners(AuditingEntityListener.class)
public class Enrollment extends BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime enrollmentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonBackReference
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonBackReference
    private Course course;

    public void linkStudentAndCourse(Student student, Course course) {
        this.student = student;
        this.course = course;

        student.getEnrollments().add(this);
        course.getEnrollments().add(this);
    }

    public void unlinkStudentAndCourse(Student student, Course course) {
        if(this.student == student && this.course == course) {
            this.student.getEnrollments().remove(this);
            this.course.getEnrollments().remove(this);

            this.student = null;
            this.course = null;
        }
    }

}
