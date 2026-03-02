package com.fabidoces_microservices.client_service.service;

import com.fabidoces_microservices.client_service.model.entity.Address;
import com.fabidoces_microservices.client_service.repository.AddressRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public Address saveAddress(Address address) {
        if (address.getClientId() == null) {
            throw new IllegalArgumentException("Id_Cliente não pode ser nulo");
        }
        return addressRepository.save(address);
    }

    public List<Address> findAll() {
        return addressRepository.findAll();
    }

    public Optional<Address> findById(Long id) {
        return addressRepository.findById(id);
    }

    public List<Address> findByClientId(Long clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("ClientId não pode ser nulo");
        }
        return addressRepository.findByClientId(clientId);
    }

    @Transactional
    public Address updateAddress(Long id, Address address) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }

        Address existingAddress = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado com id: " + id));

        existingAddress.setAddressName(address.getAddressName());
        existingAddress.setZipCode(address.getZipCode());
        existingAddress.setStreet(address.getStreet());
        existingAddress.setNeighborhood(address.getNeighborhood());
        existingAddress.setCity(address.getCity());
        existingAddress.setState(address.getState());

        return addressRepository.save(existingAddress);
    }

    @Transactional
    public boolean deleteAddress(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }

        if (addressRepository.existsById(id)) {
            addressRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteAddressesByClientId(Long clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("ClientId não pode ser nulo");
        }

        List<Address> clientAddresses = addressRepository.findByClientId(clientId);
        if (!clientAddresses.isEmpty()) {
            addressRepository.deleteAll(clientAddresses);
            return true;
        }
        return false;
    }
}