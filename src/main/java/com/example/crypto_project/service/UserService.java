package com.example.crypto_project.service;

import com.example.crypto_project.dto.RegisterRequest;
import com.example.crypto_project.model.User;
import com.example.crypto_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerNewUser(RegisterRequest registerRequest) {
        if (userRepository.findByLogin(registerRequest.getLogin()).isPresent()) {
            throw new IllegalStateException("User with login " + registerRequest.getLogin() + " already exists.");
        }

        User user = new User();
        user.setLogin(registerRequest.getLogin());
        // Хэшируем пароль перед сохранением!
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + username));

        return new org.springframework.security.core.userdetails.User(user.getLogin(), user.getPasswordHash(), new ArrayList<>());
    }
}