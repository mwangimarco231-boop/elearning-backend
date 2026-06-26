package com.marcoscode.elearning.config;


import com.marcoscode.elearning.user.Role;
import com.marcoscode.elearning.user.User;
import com.marcoscode.elearning.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
public class InitialAdminSeederConfig {


    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    CommandLineRunner commandLineRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            //     AUTOMATED FIRS ADMIN INITIALIZER


            if(!userRepository.existsByEmail(adminEmail)) {
                var initialAdmin = User.builder()
                        .firstName("System")
                        .lastName("Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .build();

                userRepository.save(initialAdmin);
                log.info("Initial administrator account created");
            }
        };
    }
}
