package com.bolao2027.modules.identity.web;

import com.bolao2027.modules.identity.application.EmailService;
import com.bolao2027.modules.identity.application.UserService;
import com.bolao2027.modules.identity.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * Controller para fluxos de identidade: registro de conta e recuperação de senha.
 */
@Controller
public class IdentityController {

    private final UserService userService;
    private final EmailService emailService;

    @Value("${bolao.app-url:http://localhost:8080}")
    private String appUrl;

    public IdentityController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    // ─── Registro ────────────────────────────────────────────────────────────

    /**
     * GET /register — exibe o formulário de cadastro.
     */
    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    /**
     * POST /register — processa o cadastro de novo participante.
     * Em caso de sucesso redireciona para /login?registered.
     * Em caso de erro (username/email duplicado) re-exibe o formulário com mensagem.
     */
    @PostMapping("/register")
    public String registerSubmit(@RequestParam String username,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String displayName,
                                 Model model) {
        try {
            userService.registerUser(username, email, password, displayName);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            // Devolve os campos preenchidos para melhor UX (exceto senha)
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("displayName", displayName);
            return "auth/register";
        }
    }

    // ─── Recuperação de senha ─────────────────────────────────────────────────

    /**
     * GET /forgot-password — exibe o formulário de recuperação de senha.
     * Se o parâmetro 'sent' estiver presente, exibe mensagem de sucesso.
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "auth/forgot-password";
    }

    /**
     * POST /forgot-password — processa a solicitação de recuperação de senha.
     * Sempre redireciona para /forgot-password?sent para não revelar se o e-mail existe.
     */
    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email) {
        try {
            String token = userService.createPasswordResetToken(email);
            if (token != null) {
                // Busca o displayName do usuário para personalizar o e-mail
                Optional<User> userOpt = userService.findByEmail(email);
                String displayName = userOpt.map(User::getDisplayName).orElse(email);
                emailService.sendPasswordResetEmail(email, displayName, token, appUrl);
            }
            // Sempre redireciona — não revela se o e-mail está cadastrado
        } catch (Exception e) {
            // Silencia qualquer erro para não revelar informações ao atacante
        }
        return "redirect:/forgot-password?sent";
    }

    /**
     * GET /reset-password?token= — exibe o formulário de redefinição de senha.
     */
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    /**
     * POST /reset-password — processa a redefinição de senha com token.
     * Valida se as senhas coincidem antes de chamar o serviço.
     */
    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword,
                                      Model model) {
        // Validação: senhas devem ser iguais
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "As senhas não conferem. Por favor, tente novamente.");
            return "auth/reset-password";
        }

        try {
            userService.resetPasswordWithToken(token, newPassword);
            return "redirect:/login?passwordReset";
        } catch (IllegalArgumentException e) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/reset-password";
        }
    }
}
