package com.marcoscode.elearning.section;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.marcoscode.elearning.common.domain.BaseEntity;
import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.lecture.Lecture;
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
@Table(name = "section_t", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"title", "course_id"}),
        @UniqueConstraint(columnNames = {"order_index", "course_id"})
})

public class Section extends BaseEntity {

    private Integer orderIndex;
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonBackReference
    private Course course;

    @OneToMany(
            mappedBy = "section",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @JsonManagedReference
    private List<Lecture> lectures = new ArrayList<>();


    public void addLecture(Lecture lecture) {
        this.lectures.add(lecture);
        lecture.setSection(this);
    }

    public void removeLecture(Lecture lecture) {
        this.lectures.remove(lecture);
        lecture.setSection(null);
    }
}
