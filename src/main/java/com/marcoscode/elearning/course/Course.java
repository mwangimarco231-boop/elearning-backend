package com.marcoscode.elearning.course;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.marcoscode.elearning.common.domain.BaseEntity;
import com.marcoscode.elearning.enrollment.Enrollment;
import com.marcoscode.elearning.section.Section;
import com.marcoscode.elearning.instructor.Instructor;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

@Entity
@Table(name = "courses", uniqueConstraints =
    @UniqueConstraint(columnNames = {"title", "instructor_id", "course_level"}))

public class Course extends BaseEntity {

    @Column(nullable = false)
    private  String title;
    private Double price;

    @Enumerated(EnumType.STRING)
    private Level courseLevel;


    @OneToMany(
            mappedBy = "course",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    @Builder.Default
    @JsonManagedReference
    private List<Enrollment> enrollments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "instructor_id",
            nullable = false
    )

    @JsonBackReference
    private Instructor instructor;

    @OneToMany(
            mappedBy = "course",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    @Builder.Default
    @JsonManagedReference
    private List<Section> sections = new ArrayList<>();

    public void addSection(Section section) {
        this.sections.add(section);
        section.setCourse(this);
    }

    public void removeSection(Section section){
        this.sections.remove(section);
        section.setCourse(null);
    }
}