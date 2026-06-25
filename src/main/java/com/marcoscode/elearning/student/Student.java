package com.marcoscode.elearning.student;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.marcoscode.elearning.enrollment.Enrollment;
import com.marcoscode.elearning.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


import java.util.HashSet;
import java.util.Set;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

@PrimaryKeyJoinColumn(name = "student_id")
@Entity
@Table(name = "students")
public class Student extends User {


    @OneToMany(
            mappedBy = "student",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    @Builder.Default
    @JsonManagedReference
    private Set<Enrollment> enrollments = new HashSet<>();

}
