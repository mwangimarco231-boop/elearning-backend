package com.marcoscode.elearning.user;


import com.marcoscode.elearning.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity

@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "f_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "l_name", nullable = false, length = 50)
    private String lastName;

    //private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
