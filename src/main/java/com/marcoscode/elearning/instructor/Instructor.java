package com.marcoscode.elearning.instructor;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

@PrimaryKeyJoinColumn(name = "instructor_id")
@Entity
@Table(name = "instructors")
public class Instructor extends User {

    @Column(columnDefinition = "TEXT")
    private String bio;

    @OneToMany(mappedBy = "instructor",
            fetch =  FetchType.LAZY,
            cascade = {CascadeType.PERSIST,
                        CascadeType.MERGE}
                )

    @JsonManagedReference
    @Builder.Default
    private List<Course> courses =  new ArrayList<>();


    public void addCourse(Course course) {
        this.courses.add(course);
        course.setInstructor(this);
    }

    public void removeCourse(Course course) {
        this.courses.remove(course);
        course.setInstructor(null);
    }
}
