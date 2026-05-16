package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.seed-admin.enabled:true}")
	private boolean enabled;

	@Value("${app.seed-admin.email:admin@demo.local}")
	private String email;

	@Value("${app.seed-admin.password:Admin123!}")
	private String password;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!enabled) {
			return;
		}

		String normalizedEmail = email.trim().toLowerCase();
		User admin = userRepository.findByEmail(normalizedEmail).orElse(null);
		if (admin == null) {
			admin = new User();
			admin.setEmail(normalizedEmail);
			admin.setPasswordHash(passwordEncoder.encode(password));
			admin.setCreatedAt(Instant.now());
			admin.setRole("ADMIN");
		} else if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
			admin.setRole("ADMIN");
		}
		admin.setUpdatedAt(Instant.now());
		userRepository.save(admin);
	}
}