package com.test.usermanagementservice;


import com.test.usermanagementservice.models.AppUser;
import com.test.usermanagementservice.service.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findAll().isEmpty()) {
                userRepository.save(new AppUser( "admin", passwordEncoder.encode("admin123"), "ADMIN"));
                userRepository.save(new AppUser("user", passwordEncoder.encode("user123"), "USER"));
            }
        };
    }
}
