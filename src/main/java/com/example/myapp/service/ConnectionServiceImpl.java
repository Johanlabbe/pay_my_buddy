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
import java.util.Objects;
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
    @Transactional(readOnly = true)
    public List<ConnectionDTO> getConnections(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé : " + userId));

        return connectionRepository.findByUser(user)
                .stream()
                .map(conn -> new ConnectionDTO(user.getId(), conn.getConnection().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public ConnectionDTO add(ConnectionDTO dto) {
        if (dto == null || dto.getUserId() == null || dto.getFriendId() == null) {
            throw new IllegalArgumentException("userId et friendId sont obligatoires");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé : " + dto.getUserId()));
        User friend = userRepository.findById(dto.getFriendId())
                .orElseThrow(() -> new EntityNotFoundException("Ami non trouvé : " + dto.getFriendId()));

        if (Objects.equals(user.getId(), friend.getId())) {
            throw new IllegalArgumentException("Impossible de se connecter à soi-même");
        }

        boolean exists = connectionRepository.existsByUserAndConnection(user, friend);
        if (exists) {
            throw new IllegalStateException("Connexion déjà existante");
        }

        UserConnection uc = new UserConnection();
        uc.setUser(user);
        uc.setConnection(friend);

        UserConnection saved = connectionRepository.save(uc);
        return new ConnectionDTO(saved.getUser().getId(), saved.getConnection().getId());
    }

    @Override
    public void remove(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new IllegalArgumentException("userId et friendId sont obligatoires");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé : " + userId));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Ami non trouvé : " + friendId));

        UserConnection uc = connectionRepository.findByUserAndConnection(user, friend)
                .orElseThrow(() -> new EntityNotFoundException("Connexion non trouvée"));
        connectionRepository.delete(uc);
    }
}
