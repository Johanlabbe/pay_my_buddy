package com.example.myapp.service;

import com.example.myapp.dto.CreateUserDTO;
import com.example.myapp.dto.UserDTO;

public interface UserService {
    UserDTO create(CreateUserDTO dto);

    /**
     * Met à jour l’utilisateur identifié par userId en modifiant
     * uniquement le ou les champs autorisés (ici, par exemple, username et/ou password).
     */
    UserDTO update(Long userId, CreateUserDTO dto);

    void delete(Long id);

    UserDTO findById(Long id);
}
