package com.skillexchange.service;

import com.skillexchange.dto.LoginRequest;
import com.skillexchange.dto.RegisterRequest;
import com.skillexchange.model.User;
import com.skillexchange.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("A user with this email already exists");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setLocation(req.getLocation());
        user.setSkillsOffered(req.getSkillsOffered() != null ? req.getSkillsOffered() : new ArrayList<>());
        user.setSkillsWanted(req.getSkillsWanted() != null ? req.getSkillsWanted() : new ArrayList<>());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> authenticate(LoginRequest req) {
        return userRepository.findByEmailIgnoreCase(req.getEmail())
                .filter(u -> passwordEncoder.matches(req.getPassword(), u.getPasswordHash()));
    }
}
