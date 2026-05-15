package com.example.demo.controller;

import com.example.demo.dto.MeResponse;
import com.example.demo.repository.UserRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MeController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public MeResponse me(Principal principal) {
        String email = principal.getName();
        return userRepository.findByEmail(email)
                .map(user -> new MeResponse(user.getId(), user.getEmail()))
                .orElseThrow();
    }
}