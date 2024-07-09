package com.example.webchat.service;

import com.example.webchat.dto.UserStatusDTO;
import com.example.webchat.repository.UserRepository;
import com.example.webchat.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }
    public User save(User user) {
        return userRepository.save(user);
    }

    public List<UserStatusDTO> getAllUserStatuses() {
        return userRepository.findAll().stream()
                .map(user -> new UserStatusDTO(user.getUsername(), user.getStatus()))
                .collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}





