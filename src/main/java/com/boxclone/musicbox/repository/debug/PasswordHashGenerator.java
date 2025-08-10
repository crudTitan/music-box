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
        System.out.println("changeme1 -> " + passwordEncoder.encode("changeme1"));
        System.out.println("changeme2 -> " + passwordEncoder.encode("changeme2"));
        System.out.println("changeme3 -> " + passwordEncoder.encode("changeme3"));
    }
}
