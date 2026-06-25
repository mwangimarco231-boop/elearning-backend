package com.marcoscode.elearning;

import com.github.javafaker.Faker;
import com.marcoscode.elearning.course.Course;
import com.marcoscode.elearning.course.CourseRepository;
import com.marcoscode.elearning.course.Level;
import com.marcoscode.elearning.enrollment.Enrollment;
import com.marcoscode.elearning.lecture.Lecture;
import com.marcoscode.elearning.resource.Resource;
import com.marcoscode.elearning.resource.ResourceType;
import com.marcoscode.elearning.section.Section;
import com.marcoscode.elearning.instructor.Instructor;
import com.marcoscode.elearning.instructor.InstructorRepository;
import com.marcoscode.elearning.student.Student;
import com.marcoscode.elearning.student.StudentRepository;
import com.marcoscode.elearning.user.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DataSeederConfig {

//    @Bean
    CommandLineRunner commandLineRunner(CourseRepository courseRepository,
                                        InstructorRepository instructorRepository,
                                        StudentRepository studentRepository,
                                        PasswordEncoder passwordEncoder) {
        return args -> {
            //     AUTOMATED FIRS ADMIN INITIALIZER
            String adminEmail = "admin@elearning.com";
            String adminPassword = "adminPassword123";

            if(!studentRepository.existsByEmail(adminEmail)) {
                var initialAdmin = Student.builder()
                        .firstName("System")
                        .lastName("Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .build();

                studentRepository.save(initialAdmin);
                System.out.println("First System Admin initialized successfully");
            }

            String instructorEmail = "instructor@learning.com";
            String instructorPassword = "instructorPassword123";


            //AUTOMATED FIRST INSTRUCTOR INITIALIZER
            if (!instructorRepository.existsByEmail(instructorEmail)) {
                var initialInstructor = Instructor.builder()
                        .firstName("System")
                        .lastName("Instructor")
                        .email(instructorEmail)
                        .password(passwordEncoder.encode(instructorPassword))
                        .role(Role.INSTRUCTOR)
                        .build();

                instructorRepository.save(initialInstructor);
                System.out.println("First Instructor initialized successfully");
            }

            String studentEmail = "student@learning.com";
            String studentPassword = "studentPassword123";


            //AUTOMATED FIRST STUDENT INITIALIZER
            if (!studentRepository.existsByEmail(studentEmail)) {
                var initialStudent = Student.builder()
                        .firstName("System")
                        .lastName("Student")
                        .email(studentEmail)
                        .password(passwordEncoder.encode(studentPassword))
                        .role(Role.STUDENT)
                        .build();

                studentRepository.save(initialStudent);
                System.out.println("First Student initialized successfully");
            }

            Faker faker = new Faker();

            // create and save Instructor
            for (int i = 1; i <= 10; i++) {
                var instructor = Instructor.builder()
                        .email(faker.internet().emailAddress())
                        .password(faker.internet().password())
                        .firstName(faker.name().firstName())
                        .lastName(faker.name().lastName())
                        .role(faker.options().option(
                                Role.INSTRUCTOR,
                                Role.STUDENT
                        ))
                        .build();

                instructorRepository.save(instructor);

                // create course
                var course = Course.builder()
                        .title(faker.book().title())
                        .price(faker.number().randomDouble(2, 2, 30))
                        .instructor(instructor)
                        .courseLevel(faker.options().option(
                                Level.ADVANCED,
                                Level.BEGINNER,
                                Level.INTERMEDIATE))
                        .build();

                instructor.addCourse(course);

                 //dummy section for course
                for (int s = 1; s <= 2; s++) {
                    var section = Section.builder()
                            .orderIndex(s)
                            .title(faker.book().title())
                            .build();

                    course.addSection(section);


                    // dummy lecture for each section
                    for (int l = 1; l <= 3; l++) {
                        var lecture = Lecture.builder()
                                .title(faker.book().title())
                                .durationSeconds(faker.number().numberBetween(234, 1230))
                                .build();

                        section.addLecture(lecture);

                        var enrollment = Enrollment.builder()
                                .build();

                        var student = Student.builder()
                                .firstName(faker.name().firstName())
                                .lastName(faker.name().lastName())
                                .email(faker.internet().emailAddress())
                                .password(faker.internet().password())
                                .role(faker.options().option(
                                        Role.INSTRUCTOR,
                                        Role.STUDENT
                                ))
                                .build();
                        studentRepository.save(student);

                        enrollment.linkStudentAndCourse(student, course);

                        for (int m = 1; m <= 3; m++) {
                            var resource =  Resource.builder()
                                    .fileUrl(faker.internet().url())
                                    .title(faker.book().title())
                                    .resourceType(faker.options().option(
                                            ResourceType.VIDEO,
                                            ResourceType.FILE,
                                            ResourceType.TEXT))
                                    .build();

                            lecture.addResource(resource);
                        }
                        // dummy for resource for each lecture

                    }

                }
                courseRepository.save(course);

            }

        };
    }
}
