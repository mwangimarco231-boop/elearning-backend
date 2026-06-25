package com.marcoscode.elearning.resource;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.marcoscode.elearning.common.domain.BaseEntity;
import com.marcoscode.elearning.lecture.Lecture;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

@Entity
@Table(name = "resource_t",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_lecture_file_url",
                columnNames = {"lecture_id", "file_url"}
)})
public class Resource extends BaseEntity {
    private String title;

    private String fileUrl;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id",  nullable = false)
    @JsonBackReference
    private Lecture lecture;

}
