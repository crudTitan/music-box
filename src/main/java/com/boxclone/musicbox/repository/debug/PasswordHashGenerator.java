package com.boxclone.musicbox.repository.debug;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashGenerator implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    public PasswordHashGenerator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
    }
}
