package com.bolao2027.modules.identity.infrastructure;

import com.bolao2027.modules.identity.domain.Role;
import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.identity.infrastructure.persistence.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class UserDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            createUser("admin", "admin@bolao2027.com", "admin123", "Administrador do Bolão", Role.ROLE_ADMIN);
            createUser("marina", "marina@bolao2027.com", "marina123", "Marina Silva", Role.ROLE_USER);
            createUser("carlos", "carlos@bolao2027.com", "carlos123", "Carlos Oliveira", Role.ROLE_USER);
            createUser("beatriz", "beatriz@bolao2027.com", "beatriz123", "Beatriz Santos", Role.ROLE_USER);
            createUser("joao", "joao@bolao2027.com", "joao123", "João Souza", Role.ROLE_USER);
        }
    }

    private void createUser(String username, String email, String rawPassword, String displayName, Role role) {
        User user = new User(username, email, passwordEncoder.encode(rawPassword), displayName, role);
        userRepository.save(user);
    }
}
