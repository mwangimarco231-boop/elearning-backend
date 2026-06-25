package com.marcoscode.elearning.lecture;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.marcoscode.elearning.common.domain.BaseEntity;
import com.marcoscode.elearning.resource.Resource;
import com.marcoscode.elearning.section.Section;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

@Entity
@Table(name = "lecture_t",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_lecture_title_duration_section",
                columnNames = {"title", "duration_seconds", "section_id"}
        )
})
public class Lecture  extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "section_id",
            nullable = false
    )
    @JsonBackReference
    private Section section;

    @OneToMany(
            mappedBy = "lecture",
            fetch =  FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @JsonManagedReference
    private List<Resource> resources =  new ArrayList<>();

    public void addResource(Resource resource) {
        if (resources != null) {
            this.resources.add(resource);
            resource.setLecture(this);
        }
    }

    public void removeResource(Resource resource) {
        if (resources != null) {
            this.resources.remove(resource);
            resource.setLecture(null);
        }
    }

}
