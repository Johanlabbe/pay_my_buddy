package com.example.myapp.service;

import com.example.myapp.dto.ConnectionDTO;
import com.example.myapp.entity.User;
import com.example.myapp.entity.UserConnection;
import com.example.myapp.repository.UserConnectionRepository;
import com.example.myapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConnectionServiceImpl implements ConnectionService {

    private final UserRepository userRepository;
    private final UserConnectionRepository connectionRepository;

    public ConnectionServiceImpl(UserRepository userRepository,
            UserConnectionRepository connectionRepository) {
        this.userRepository = userRepository;
        this.connectionRepository = connectionRepository;
    }

    @Override
    public List<ConnectionDTO> getConnections(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé : " + userId));

        return connectionRepository
                .findByUser(user)
                .stream()
                .map(conn -> {
                    ConnectionDTO dto = new ConnectionDTO();
                    dto.setUserId(user.getId());
                    dto.setConnectionId(conn.getConnection().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ConnectionDTO add(ConnectionDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé : " + dto.getUserId()));
        User connection = userRepository.findById(dto.getConnectionId())
                .orElseThrow(() -> new EntityNotFoundException("Ami non trouvé : " + dto.getConnectionId()));

        if (user.equals(connection)) {
            throw new IllegalArgumentException("Impossible de se connecter à soi-même");
        }
        if (connectionRepository.existsByUserAndConnection(user, connection)) {
            throw new IllegalStateException("Connexion déjà existante");
        }

        UserConnection uc = new UserConnection();
        uc.setUser(user);
        uc.setConnection(connection);
        UserConnection saved = connectionRepository.save(uc);

        ConnectionDTO result = new ConnectionDTO();
        result.setUserId(saved.getUser().getId());
        result.setConnectionId(saved.getConnection().getId());
        return result;
    }

    @Override
    public void remove(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé : " + userId));
        User connection = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Ami non trouvé : " + friendId));

        UserConnection uc = connectionRepository
                .findByUserAndConnection(user, connection)
                .orElseThrow(() -> new EntityNotFoundException("Connexion non trouvée"));
        connectionRepository.delete(uc);
    }
}
