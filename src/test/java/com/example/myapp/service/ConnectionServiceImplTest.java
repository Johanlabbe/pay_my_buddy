package com.example.myapp.service;

import com.example.myapp.dto.ConnectionDTO;
import com.example.myapp.entity.User;
import com.example.myapp.entity.UserConnection;
import com.example.myapp.repository.UserConnectionRepository;
import com.example.myapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserConnectionRepository connectionRepository;

    private ConnectionServiceImpl service;

    private User me;
    private User friend;

    @BeforeEach
    void setUp() {
        service = new ConnectionServiceImpl(userRepository, connectionRepository);

        me = new User();
        me.setId(1L);
        me.setEmail("me@mail.com");
        me.setUsername("me");

        friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@mail.com");
        friend.setUsername("friend");
    }

    /* ---------- getConnections() ---------- */

    @Test
    void getConnections_shouldListMyFriends_asDTO_withFriendFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));

        UserConnection uc = new UserConnection();
        uc.setUser(me);
        uc.setConnection(friend);

        when(connectionRepository.findByUser(me)).thenReturn(List.of(uc));

        List<ConnectionDTO> result = service.getConnections(1L);

        assertEquals(1, result.size());
        ConnectionDTO dto = result.get(0);
        assertEquals(1L, dto.getUserId());
        assertEquals(2L, dto.getFriendId());
        assertEquals("friend@mail.com", dto.getFriendEmail());
        assertEquals("friend", dto.getFriendName());
    }

    @Test
    void getConnections_shouldFail_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getConnections(99L));
    }

    /* ---------- add() ---------- */

    @Test
    void add_shouldCreateConnection_andReturnDTO() {
        ConnectionDTO input = new ConnectionDTO();
        input.setUserId(1L);
        input.setFriendId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(connectionRepository.existsByUserAndConnection(me, friend)).thenReturn(false);

        when(connectionRepository.save(any(UserConnection.class)))
                .thenAnswer(inv -> (UserConnection) inv.getArgument(0));

        ConnectionDTO out = service.add(input);

        assertNotNull(out);
        assertEquals(1L, out.getUserId());
        assertEquals(2L, out.getFriendId());
        assertEquals("friend@mail.com", out.getFriendEmail());
        assertEquals("friend", out.getFriendName());

        ArgumentCaptor<UserConnection> captor = ArgumentCaptor.forClass(UserConnection.class);
        verify(connectionRepository).save(captor.capture());
        UserConnection persisted = captor.getValue();
        assertEquals(me, persisted.getUser());
        assertEquals(friend, persisted.getConnection());
    }

    @Test
    void add_shouldFail_whenSelfConnection() {
        ConnectionDTO input = new ConnectionDTO();
        input.setUserId(1L);
        input.setFriendId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(me));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.add(input));
        assertTrue(ex.getMessage().toLowerCase().contains("soi"));
        verify(connectionRepository, never()).save(any());
    }

    @Test
    void add_shouldFail_whenAlreadyExists() {
        ConnectionDTO input = new ConnectionDTO();
        input.setUserId(1L);
        input.setFriendId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(connectionRepository.existsByUserAndConnection(me, friend)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.add(input));
        verify(connectionRepository, never()).save(any());
    }

    @Test
    void add_shouldFail_whenUserOrFriendMissing() {
        ConnectionDTO input = new ConnectionDTO();
        input.setUserId(1L);
        input.setFriendId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.add(input));

        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.add(input));
    }

    /* ---------- remove() ---------- */

    @Test
    void remove_shouldDeleteConnection() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));

        UserConnection uc = new UserConnection();
        uc.setUser(me);
        uc.setConnection(friend);

        when(connectionRepository.findByUserAndConnection(me, friend)).thenReturn(Optional.of(uc));

        service.remove(1L, 2L);

        verify(connectionRepository).delete(uc);
    }

    @Test
    void remove_shouldFail_whenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(connectionRepository.findByUserAndConnection(me, friend)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.remove(1L, 2L));
        verify(connectionRepository, never()).delete(any());
    }

    @Test
    void remove_shouldFail_whenUserOrFriendMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.remove(1L, 2L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.remove(1L, 2L));
    }
}
