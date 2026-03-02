package com.fabidoces_microservices.client_service.service;

import com.fabidoces_microservices.client_service.config.PasswordConfig;
import com.fabidoces_microservices.client_service.events.dto.ClientQueryEvent;
import com.fabidoces_microservices.client_service.events.dto.ClientResponseEvent;
import com.fabidoces_microservices.client_service.events.EmailEvent;
import com.fabidoces_microservices.client_service.model.dto.CartDTO;
import com.fabidoces_microservices.client_service.model.dto.ClientResponse;
import com.fabidoces_microservices.client_service.model.dto.ClientSaved;
import com.fabidoces_microservices.client_service.model.entity.Cart;
import com.fabidoces_microservices.client_service.model.entity.Client;
import com.fabidoces_microservices.client_service.repository.CartRepository;
import com.fabidoces_microservices.client_service.repository.ClientRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PasswordConfig passwordEncoder;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.default-send}")
    private String to;

    @Value("${app.rabbitmq.queues.client.response}")
    private String clientResponseQueue;

    @Value("${app.rabbitmq.queues.email.name:emailQueue}")
    private String emailQueue;



    public ClientSaved saveClient(Client client) {
        client.setSenha(passwordEncoder.passwordEncoder().encode(client.getSenha()));
        Client savedClient = clientRepository.save(client);

        EmailEvent event = new EmailEvent(savedClient.getEmail(), savedClient.getNome(), savedClient.getId());

        sendConfirmationEmail(savedClient);

        return new ClientSaved(savedClient.getId(), savedClient.getCpf(), savedClient.getNome(),
                savedClient.getEmail(), savedClient.getTelefone(), 0, 0);
    }

    public void confirmarCadastro(String token) {
        Long clientId = validateToken(token);

        Client client = clientRepository.findById(clientId).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        client.setStatusCliente(1);
        clientRepository.save(client);
    }

    private Long validateToken(String token) {

        try {
            return Long.parseLong(token);
        } catch (Exception e) {
            throw new RuntimeException("Token inválido");
        }
    }

    public int changePassword(long id, String newPassword, String oldPassword) {
        Optional<Client> client = clientRepository.findById(id);

        if (client.isPresent()) {
            Client clientObj = client.get();

            if (passwordEncoder.passwordEncoder().matches(oldPassword, clientObj.getSenha())) {
                String encodedNewPassword = passwordEncoder.passwordEncoder().encode(newPassword);
                clientRepository.updatePasswordById(id, encodedNewPassword);
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public List<Client> retrieveData(){
        return clientRepository.findAll();
    }

    public ClientResponse retrieveLoginData(String email){
        Optional<Client> rawData =  clientRepository.findByEmail(email);
        ClientResponse loginData = new ClientResponse();

        if (rawData.isPresent()){
            loginData.setId(rawData.get().getId());
            loginData.setNome(rawData.get().getNome());
            loginData.setEmail(email);
            loginData.setTelefone(rawData.get().getTelefone());
            loginData.setStatus(rawData.get().getStatusCliente());
        }
        return loginData;
    }

    public boolean activateClient(Long id){
        if (clientRepository.findById(id).isPresent()){
            clientRepository.atualizaStatusCliente(id);
            return true;
        } else {
            return false;
        }
    }

    private boolean validatePassword(String rawPassword, String encodedPassword){
        return passwordEncoder.passwordEncoder().matches(rawPassword,encodedPassword);
    }

    public boolean authorizeLogin(String email, String inputPassword){
        Optional<Client> optClient = clientRepository.findByEmail(email);
        Client client;

        if (optClient.isPresent()){
             client = optClient.get();

             if (client.getSenha() != null){
                 return validatePassword(inputPassword,client.getSenha());
             }
        }
        return false;
    }


    public Optional<List<Cart>> cartItens(Long clientId) {
        if (clientId == null) {
            return Optional.empty();
        }
        return cartRepository.findAllByClientId(clientId);
    }

    @Transactional
    public void insertIntoCart(CartDTO cart) {
        if (cart.getClientId() == null || cart.getProductId() == null) {
            throw new IllegalArgumentException("Id_Cliente ou Id_produto não podem ser nulos");
        }

        cartRepository.updateOrInsert(
                cart.getClientId(),
                cart.getProductId(),
                cart.getQuantity()
        );
    }

    @Transactional
    public void insertMultipleIntoCart(List<CartDTO> clientCart) {
        for (CartDTO cart : clientCart) {
            if (cart.getClientId() == null || cart.getProductId() == null) {
                throw new IllegalArgumentException("Id Cliente ou Id produto não podem ser nulos");
            }

            cartRepository.updateOrInsert(
                    cart.getClientId(),
                    cart.getProductId(),
                    cart.getQuantity()
            );
        }
    }

    @Transactional
    public boolean deleteFromCart(Long clientId, Long productId) {
        if (clientId == null || productId == null) {
            throw new IllegalArgumentException("ClientId e ProductId não podem ser nulos");
        }

        if (cartRepository.existsByClientIdAndProductId(clientId, productId)) {
            cartRepository.deleteByClientIdAndProductId(clientId, productId);
            return true;
        }
        return false;
    }


    @Transactional
    public void clearClientCart(Long clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("ClientId não pode ser nulo");
        }

        cartRepository.deleteAllByClientId(clientId);
    }


    // Emails (temporário)

    private void sendConfirmationEmail(Client client) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(this.from);
            helper.setTo(client.getEmail());
            helper.setSubject("Confirme seu cadastro - NexBuy 3D");

            String token = generateConfirmationToken(client.getId());
            String primeiroNome = client.getNome().split(" ")[0];

            String confirmationLink ="http://192.168.0.12:8080/api/client/confirmar-cadastro?token=" + token;
            String htmlContent = loadEmailTemplate(primeiroNome, confirmationLink);
            helper.setText(htmlContent, true);

            emailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar email de confirmação");
        }
    }

    private String loadEmailTemplate(String nome, String confirmationLink) {
        try {
            Resource resource = new ClassPathResource("templates/confirmacao.html");
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            return template
                    .replace("[[${nome}]]", nome)
                    .replace("${linkConfirmacao}", confirmationLink)
                    .replace("th:href=\"${linkConfirmacao}\"", "href=\"" + confirmationLink + "\"");

        } catch (Exception e) {
            return "";
        }
    }


    private String generateConfirmationToken(Long clientId) {
        return UUID.randomUUID().toString() + "_" + clientId;
    }


    //Pagamentos e afins
    public void sendClientResponse(ClientResponseEvent responseEvent) {
        try {
            rabbitTemplate.convertAndSend(clientResponseQueue, responseEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
