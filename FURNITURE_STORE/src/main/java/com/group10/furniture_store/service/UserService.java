package com.group10.furniture_store.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.group10.furniture_store.domain.Role;
import com.group10.furniture_store.domain.User;
import com.group10.furniture_store.repository.RoleRepository;
import com.group10.furniture_store.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> getAllUser() {
        return this.userRepository.findAll();
    }

    public User handleSaveUser(User x) {
        User tmp = this.userRepository.save(x);
        return tmp;
    }

    public User getUserById(Long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        User x = userOptional.isPresent() ? userOptional.get() : null;
        return x;
    }

    public void deleteAnUser(Long id) {
        this.userRepository.deleteById(id);
    }

    public Role getRoleByName(String name) {
        return this.roleRepository.findByName(name);
    }
}
