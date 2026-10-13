package com.bolao2027.modules.identity.application;

import com.bolao2027.modules.identity.domain.PasswordResetToken;
import com.bolao2027.modules.identity.domain.Role;
import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.identity.infrastructure.persistence.PasswordResetTokenRepository;
import com.bolao2027.modules.identity.infrastructure.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordResetTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String email, String rawPassword, String displayName) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Nome de usuário já está em uso.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-mail já está cadastrado.");
        }

        String encoded = passwordEncoder.encode(rawPassword);
        User user = new User(username, email, encoded, displayName, Role.ROLE_USER);
        return userRepository.save(user);
    }

    public void changePassword(Long userId, String currentRawPassword, String newRawPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!passwordEncoder.matches(currentRawPassword, user.getPassword())) {
            throw new IllegalArgumentException("A senha atual informada está incorreta.");
        }

        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }

    public String createPasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return null;
        }
        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusHours(1));
        tokenRepository.save(resetToken);
        return token;
    }

    public void resetPasswordWithToken(String token, String newRawPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de recuperação inválido."));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Este token de recuperação já foi utilizado.");
        }
        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Este token de recuperação expirou.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
