package com.fabidoces_microservices.client_service.controller;

import com.fabidoces_microservices.client_service.events.dto.ClientResponseEvent;
import com.fabidoces_microservices.client_service.model.dto.CartDTO;
import com.fabidoces_microservices.client_service.model.dto.ClientProfileImageDTO;
import com.fabidoces_microservices.client_service.model.dto.ClientResponse;
import com.fabidoces_microservices.client_service.model.dto.ClientSaved;
import com.fabidoces_microservices.client_service.model.entity.Address;
import com.fabidoces_microservices.client_service.model.entity.Cart;
import com.fabidoces_microservices.client_service.model.entity.Client;
import com.fabidoces_microservices.client_service.repository.ClientRepository;
import com.fabidoces_microservices.client_service.service.AddressService;
import com.fabidoces_microservices.client_service.service.ClientImageService;
import com.fabidoces_microservices.client_service.service.ClientService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientImageService imageService;

    @Autowired
    private ClientRepository clientRepository;


    @PostMapping("/cadastro")
    public ClientSaved postAddClient(@RequestBody Client client){
        return clientService.saveClient(client);
    }

    @GetMapping("/clients")
    public List<ClientResponse> getAllClients(){
        List<Client> list = clientService.retrieveData();
        return list.stream().map(client -> new ClientResponse(client.getId(), client.getNome(), client.getEmail(), client.getTelefone(),client.getStatusCliente())).toList();
    }

    @GetMapping("/confirmacao")
    public void confirmRegister(@RequestParam Long id, HttpServletResponse response) throws IOException {
        boolean sucesso = clientService.activateClient(id);
        if(sucesso){
            response.sendRedirect("http://127.0.0.1:5500/fabidoces/confirmacaoCadastro.html");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ClientResponse> validateLogin(@RequestParam  String email, @RequestParam  String senha) {
        ClientResponse LoginResponse = null;
        if (clientService.authorizeLogin(email,senha)) {
            LoginResponse = clientService.retrieveLoginData(email);
            return ResponseEntity.ok(LoginResponse);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponse);
    }

    @PostMapping("/alterPassword")
    public int alterPassword(@RequestParam Long id, String password, String oldPassword){
        return clientService.changePassword(id,password,oldPassword);
    }

    @GetMapping("/image/listAllImages")
    public ResponseEntity<List<ClientProfileImageDTO>> getAllImages() {
        List<ClientProfileImageDTO> images = imageService.getAllImages();
        return ResponseEntity.ok(images);
    }

    @PostMapping("/image/upload")
    public ResponseEntity<ClientProfileImageDTO> uploadImage(@RequestParam("image") MultipartFile file, @RequestParam("clientId") Long clientId) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (!imageService.isValidImageType(file)) {
            return ResponseEntity.status(415).build(); // UNSUPPORTED_MEDIA_TYPE
        }

        ClientProfileImageDTO uploadedImage = imageService.uploadProfileImage(file,clientId);
        return ResponseEntity.ok(uploadedImage);
    }

    @GetMapping("/image/profile/{clientId}")
    public ResponseEntity<ClientProfileImageDTO> getImage(@PathVariable Long clientId) {
        Optional<ClientProfileImageDTO> image = imageService.getImageByClientId(clientId);
        return image.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cart/clientCart")
    public Optional<List<Cart>> getCart(@Param("clientId") Long clientId){
        return clientService.cartItens(clientId);
    }

    @PostMapping("/cart/insertSingle")
    public ResponseEntity<String> insertSingle(@RequestBody CartDTO cart){
         try {
             clientService.insertIntoCart(cart);
             return ResponseEntity.ok("Cart atualizado.");
         } catch (Exception e){
             return ResponseEntity.badRequest().body("Erro ao atualizar cart.");
         }
    }

    @PostMapping("/cart/insertMultiple")
    public ResponseEntity<String> insertMultiple(@RequestBody List<CartDTO> cart){
        if (cart == null || cart.isEmpty()) {
            return ResponseEntity.badRequest().body("Lista de cart vazia.");
        }
        try {
            clientService.insertMultipleIntoCart(cart);
            return ResponseEntity.ok("Cart atualizado.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("Erro interno.");
        }
    }

    @DeleteMapping("/cart/deleteItem")
    public ResponseEntity<String> deleteCartItem(
            @RequestParam Long clientId,
            @RequestParam Long productId) {
        try {
            clientService.deleteFromCart(clientId, productId);
            return ResponseEntity.ok("Item removido do carrinho.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao remover item do carrinho.");
        }
    }

    @DeleteMapping("/cart/clearCart")
    public ResponseEntity<String> clearCart(@RequestParam Long clientId) {
        try {
            clientService.clearClientCart(clientId);
            return ResponseEntity.ok("Carrinho limpo.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao limpar carrinho.");
        }
    }


    //Endereço

    @Autowired
    AddressService addressService;

    @PostMapping("/address/register")
    public ResponseEntity<?> postAddAddress(@RequestBody Address address) {
        try {
            Address savedAddress = addressService.saveAddress(address);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao cadastrar endereço");
        }
    }

    @GetMapping("/address/list")
    public ResponseEntity<List<Address>> getAllAddresses() {
        List<Address> addresses = addressService.findAll();
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/address/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable Long id) {
        Optional<Address> address = addressService.findById(id);
        return address.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/address/client/{clientId}")
    public ResponseEntity<List<Address>> getAddressesByClient(@PathVariable Long clientId) {
        try {
            List<Address> addresses = addressService.findByClientId(clientId);
            return ResponseEntity.ok(addresses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/address/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        try {
            Address updatedAddress = addressService.updateAddress(id, address);
            return ResponseEntity.ok(updatedAddress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao atualizar endereço");
        }
    }

    @DeleteMapping("/address/{id}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long id) {
        try {
            boolean deleted = addressService.deleteAddress(id);
            if (deleted) {
                return ResponseEntity.ok("Endereço deletado com sucesso");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao deletar endereço");
        }
    }

    @DeleteMapping("/address/client/{clientId}")
    public ResponseEntity<String> deleteAddressesByClient(@PathVariable Long clientId) {
        try {
            boolean deleted = addressService.deleteAddressesByClientId(clientId);
            if (deleted) {
                return ResponseEntity.ok("Endereços do cliente deletados com sucesso");
            } else {
                return ResponseEntity.ok("Nenhum endereço encontrado para este cliente");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao deletar endereços do cliente");
        }
    }

    @GetMapping("/internal/{clientId}")
    public ClientResponseEvent getClientDataInternal(@PathVariable Long clientId) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);

        ClientResponseEvent response = new ClientResponseEvent();
        response.setClientId(clientId);

        if (clientOptional.isPresent()) {
            Client client = clientOptional.get();
            response.setFound(true);
            response.setEmail(client.getEmail());

            String[] names = client.getNome().split(" ", 2);
            response.setFirstName(names[0]);
            response.setLastName(names.length > 1 ? names[1] : "");

            response.setCpf(client.getCpf());
            response.setPhone(client.getTelefone());
        } else {
            response.setFound(false);
        }

        return response;
    }

    @PostMapping("/confirmar-cadastro")
    public ResponseEntity<?> confirmarCadastro(@RequestParam String token) {
        try {
            clientService.confirmarCadastro(token);
            return ResponseEntity.ok("Cadastro confirmado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Token inválido ou expirado");
        }
    }

}
