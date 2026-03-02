package com.fabidoces_microservices.email_service.controller;

import com.fabidoces_microservices.email_service.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@Controller
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/confirmacao")
    public void defaultEmailConfirmation(@RequestParam String email, @RequestParam String nome, @RequestParam Long id) throws MessagingException {
        emailService.sendEmailConfirmation(email, nome, id);
    }
}
