package com.marcoscode.elearning.security;


import com.marcoscode.elearning.course.CourseRepository;
import com.marcoscode.elearning.course.CourseService;
import com.marcoscode.elearning.instructor.InstructorRepository;
import com.marcoscode.elearning.lecture.LectureRepository;
import com.marcoscode.elearning.resource.ResourceRepository;
import com.marcoscode.elearning.section.SectionRepository;
import com.marcoscode.elearning.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LectureRepository lectureRepository;
    private final ResourceRepository resourceRepository;


    public CustomUserDetails getCurrentUser(){
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();


        if(auth == null || !auth.isAuthenticated() || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken){
            throw new org.springframework.security.authentication.InsufficientAuthenticationException(
                    "User is not authenticated");
        }

        return (CustomUserDetails) auth.getPrincipal();
    }

    public Long getCurrentUserId(){
        return getCurrentUser().getId();
    }

    public String getCurrentUsername(){
        return getCurrentUser().getUsername();
    }


    public boolean ownsStudentProfile(Long studentId){
        return getCurrentUserId().equals(studentId);
    }

    public boolean ownsInstructorProfile(Long instructorId){
        return getCurrentUserId().equals(instructorId);
    }

    public boolean ownsCourse(Long courseId){
        return courseRepository.findById(courseId)
                .map(course ->
                        course.getInstructor().getId() != null &&
                        course.getInstructor().getId().equals(getCurrentUserId())
                )
                .orElse(false);
    }

    public boolean ownSection(Long sectionId){
        return sectionRepository.findById(sectionId)
                .map(section ->
                        ownsCourse(section.getCourse().getId()))
                .orElse(false);
    }

    public boolean ownsLecture(Long lectureId){
        return lectureRepository.findById(lectureId)
                .map(lecture ->
                        ownSection(lecture.getSection().getId()))
                .orElse(false);
    }

    public boolean ownsResource(Long resourceId){
        return resourceRepository.findById(resourceId)
                .map(resource ->
                        ownsLecture(resource.getLecture().getId()))
                .orElse(false);
    }

}
