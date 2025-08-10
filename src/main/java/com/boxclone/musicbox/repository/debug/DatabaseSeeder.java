package com.boxclone.musicbox.repository.debug;

import com.boxclone.musicbox.entity.RoleEntity;
import com.boxclone.musicbox.entity.UserEntity;
import com.boxclone.musicbox.repository.RoleRepository;
import com.boxclone.musicbox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            RoleEntity userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(RoleEntity.builder().name("ROLE_USER").build()));

            UserEntity user = UserEntity.builder()
                    .username("testuser")
                    .password(passwordEncoder.encode("pass"))
                    .roles(Set.of(userRole))
                    .build();

            userRepository.save(user);
        }
    }
}
