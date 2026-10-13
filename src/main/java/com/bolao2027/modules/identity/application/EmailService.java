package com.bolao2027.modules.identity.application;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pelo envio de e-mails transacionais.
 * <p>
 * JavaMailSender é injetado como optional (required=false) para que a aplicação
 * suba normalmente em ambientes sem SMTP configurado (testes, dev local).
 * Nesses casos o link de recuperação é apenas logado.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${bolao.mail-from:Bolão 2027 <bolao2027@example.com>}")
    private String mailFrom;

    /**
     * Envia o e-mail de recuperação de senha para o usuário.
     *
     * @param toEmail     endereço de destino
     * @param displayName nome de exibição do usuário
     * @param resetToken  token UUID gerado pelo UserService
     * @param appUrl      URL base da aplicação (ex: https://bolao2027.example.com)
     */
    public void sendPasswordResetEmail(String toEmail, String displayName,
                                       String resetToken, String appUrl) {
        String resetLink = appUrl + "/reset-password?token=" + resetToken;

        if (mailSender == null) {
            log.warn("[EmailService] JavaMailSender não configurado. "
                    + "Link de recuperação (válido por 1 hora): {}", resetLink);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject("🔑 Recuperação de senha — Bolão 2027");
            helper.setText(buildHtmlBody(displayName, resetLink), true);

            mailSender.send(message);
            log.info("[EmailService] E-mail de recuperação enviado para: {}", toEmail);

        } catch (Exception e) {
            log.error("[EmailService] Falha ao enviar e-mail de recuperação para {}: {}",
                    toEmail, e.getMessage(), e);
            // Não propaga a exceção: o fluxo de recuperação não deve revelar
            // ao usuário se o envio teve êxito ou não.
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private String buildHtmlBody(String displayName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="color-scheme" content="dark light"/>
                  <style>
                    body {
                      background-color: #0f172a;
                      color: #f1f5f9;
                      font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                      margin: 0; padding: 0;
                    }
                    .wrapper {
                      max-width: 560px;
                      margin: 40px auto;
                      background: #1e293b;
                      border-radius: 12px;
                      padding: 36px 32px;
                      border: 1px solid #334155;
                    }
                    .logo {
                      font-size: 2rem;
                      text-align: center;
                      margin-bottom: 0.5rem;
                    }
                    .title {
                      text-align: center;
                      font-size: 1.25rem;
                      font-weight: 700;
                      color: #f59e0b;
                      margin-bottom: 1.5rem;
                    }
                    p { line-height: 1.6; margin: 0 0 1rem; }
                    .btn {
                      display: inline-block;
                      margin: 1.25rem 0;
                      padding: 0.75rem 1.75rem;
                      background: #f59e0b;
                      color: #000;
                      font-weight: 700;
                      font-size: 1rem;
                      border-radius: 8px;
                      text-decoration: none;
                    }
                    .link-fallback {
                      font-size: 0.8rem;
                      color: #94a3b8;
                      word-break: break-all;
                    }
                    .footer {
                      margin-top: 2rem;
                      font-size: 0.78rem;
                      color: #64748b;
                      border-top: 1px solid #334155;
                      padding-top: 1rem;
                    }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="logo">⚽</div>
                    <div class="title">Bolão 2027 — Copa do Mundo Feminina</div>

                    <p>Olá, <strong>%s</strong>!</p>
                    <p>Recebemos uma solicitação para redefinir a senha da sua conta no Bolão 2027.
                       Clique no botão abaixo para criar uma nova senha:</p>

                    <div style="text-align:center;">
                      <a href="%s" class="btn">🔑 Redefinir minha senha</a>
                    </div>

                    <p>Se o botão não funcionar, copie e cole o link abaixo no seu navegador:</p>
                    <p class="link-fallback">%s</p>

                    <p>⏱️ Este link é válido por <strong>1 hora</strong>.</p>
                    <p>Se você não solicitou a redefinição de senha, ignore este e-mail com segurança
                       — sua senha não será alterada.</p>

                    <div class="footer">
                      Este é um e-mail automático. Por favor, não responda.
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(displayName, resetLink, resetLink);
    }
}
