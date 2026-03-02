package com.fabidoces_microservices.email_service.service;

import com.fabidoces_microservices.email_service.events.EmailEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailService {

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.default-send}")
    private String to;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Async
    public void sendEmailConfirmation(String to, String nome, Long id) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper split = new MimeMessageHelper(message, true,"UTF-8");

        split.setFrom(this.from);
        split.setTo(to);
        split.setSubject("Confirmação de Cadastro");

        Context context = new Context();
        context.setVariable("nome",nome);
        context.setVariable("linkConfirmacao","http://localhost:8080/api/client/confirmar-cadastro?id=" + id);
        String htmlContent = this.templateEngine.process("confirmacao",context);

        split.setText(htmlContent, true);
        emailSender.send(message);
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.email}")
    public void consumeEmailEvent(EmailEvent event) {
        try {
            log.info("Processando email para: {}", event.getEmail());

            Long token = generateConfirmationToken(event.getClientId());
            String confirmationLink ="http://192.168.0.12:8080/api/client/confirmar-cadastro?token=" + token;

            sendConfirmationEmail(event.getEmail(), event.getNome(), confirmationLink);

            log.info("Email de confirmação enviado com sucesso para: {}", event.getEmail());

        } catch (Exception e) {
            log.error("Erro ao processar email para {}: {}", event.getEmail(), e.getMessage());
        }
    }

    private void sendConfirmationEmail(String toEmail, String nome, String confirmationLink) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(this.from);
            helper.setTo(toEmail);
            helper.setSubject("Confirme seu cadastro - NexBuy 3D");

            // Carregar e processar template HTML
            String htmlContent = loadEmailTemplate(nome, confirmationLink);
            helper.setText(htmlContent, true);

            emailSender.send(message);

        } catch (Exception e) {
            log.error("Erro ao enviar email: {}", e.getMessage());
            throw new RuntimeException("Falha ao enviar email de confirmação");
        }
    }

    private String loadEmailTemplate(String nome, String confirmationLink) {
        try {
            Resource resource = new ClassPathResource("templates/email-confirmacao.html");
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            return template
                    .replace("[[${nome}]]", nome)
                    .replace("${linkConfirmacao}", confirmationLink)
                    .replace("th:href=\"${linkConfirmacao}\"", "href=\"" + confirmationLink + "\"");

        } catch (Exception e) {
            return "";
        }
    }

    private Long generateConfirmationToken(Long clientId) {
        return clientId;
    }

}
