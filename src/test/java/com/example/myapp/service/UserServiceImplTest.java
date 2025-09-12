// src/test/java/com/example/myapp/service/UserServiceImplTest.java
package com.example.myapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.myapp.dto.CreateUserDTO;
import com.example.myapp.dto.UpdateProfileDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import com.example.myapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Authentication authentication;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, passwordEncoder);
    }

    /* ---------- create() ---------- */

    @Test
    void create_shouldPersistUser_withLowercasedEmail_trimmedUsername_andZeroSolde() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setEmail("  NewUser@Email.COM  ");
        dto.setUsername("  John  ");
        dto.setPassword("secret");

        when(userRepository.existsByEmail("newuser@email.com")).thenReturn(false);
        when(userRepository.existsByUsername("John")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("ENC(secret)");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42L);
            return u;
        });

        UserDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals(42L, result.getId());
        assertEquals("newuser@email.com", result.getEmail());
        assertEquals("John", result.getUsername());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue();

        assertEquals("newuser@email.com", persisted.getEmail());
        assertEquals("John", persisted.getUsername());
        assertEquals("ENC(secret)", persisted.getPassword());
        assertEquals("USER", persisted.getRole());
        assertTrue(BigDecimal.ZERO.compareTo(persisted.getBalance()) == 0, "solde doit être à 0");
    }

    @Test
    void create_shouldFail_whenEmailAlreadyUsed() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setEmail("dup@mail.com");
        dto.setUsername("john");
        dto.setPassword("x");

        when(userRepository.existsByEmail("dup@mail.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldFail_whenUsernameAlreadyUsed() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setEmail("ok@mail.com");
        dto.setUsername("dup");
        dto.setPassword("x");

        when(userRepository.existsByEmail("ok@mail.com")).thenReturn(false);
        when(userRepository.existsByUsername("dup")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("nom d'utilisateur"));
        verify(userRepository, never()).save(any());
    }

    /* ---------- updateProfile() ---------- */

    @Test
    void updateProfile_shouldUpdate_trimAndLowercase_andRespectUniqueness() {
        Long userId = 7L;

        User existing = new User();
        existing.setId(userId);
        existing.setEmail("old@mail.com");
        existing.setUsername("old");
        existing.setBalance(new BigDecimal("10.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("NewName")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setEmail("  New@mail.com ");
        dto.setUsername("  NewName  ");

        UserDTO updated = service.updateProfile(userId, dto);

        assertEquals("new@mail.com", updated.getEmail());
        assertEquals("NewName", updated.getUsername());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("new@mail.com", saved.getEmail());
        assertEquals("NewName", saved.getUsername());
        assertEquals(0, saved.getBalance().compareTo(new BigDecimal("10.00")));
    }

    @Test
    void updateProfile_shouldFail_whenEmailTakenByAnother() {
        Long userId = 1L;
        User me = new User();
        me.setId(userId);
        me.setEmail("me@mail.com");
        me.setUsername("me");

        User other = new User();
        other.setId(99L);
        other.setEmail("taken@mail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(me));
        when(userRepository.findByEmail("taken@mail.com")).thenReturn(Optional.of(other));

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setEmail("taken@mail.com");
        dto.setUsername("me");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateProfile(userId, dto));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_shouldFail_whenUsernameTakenByAnother() {
        Long userId = 1L;
        User me = new User();
        me.setId(userId);
        me.setEmail("me@mail.com");
        me.setUsername("me");

        User other = new User();
        other.setId(99L);
        other.setUsername("taken");

        when(userRepository.findById(userId)).thenReturn(Optional.of(me));
        when(userRepository.findByEmail("me@mail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("taken")).thenReturn(Optional.of(other));

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setEmail("me@mail.com");
        dto.setUsername("taken");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateProfile(userId, dto));
        assertTrue(ex.getMessage().toLowerCase().contains("nom d'utilisateur"));
        verify(userRepository, never()).save(any());
    }

    /* ---------- getCurrentUser() ---------- */

    @Test
    void getCurrentUser_shouldReturnUser_whenAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("me@mail.com");

        User me = new User();
        me.setId(1L);
        me.setEmail("me@mail.com");
        when(userRepository.findByEmail("me@mail.com")).thenReturn(Optional.of(me));

        User result = service.getCurrentUser(authentication);

        assertEquals(1L, result.getId());
        assertEquals("me@mail.com", result.getEmail());
    }

    @Test
    void getCurrentUser_shouldFail_whenAuthNull() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.getCurrentUser(null));
        assertTrue(ex.getMessage().toLowerCase().contains("non authentifié"));
    }

    @Test
    void getCurrentUser_shouldFail_whenUserNotFound() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("ghost@mail.com");
        when(userRepository.findByEmail("ghost@mail.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getCurrentUser(authentication));
        assertTrue(ex.getMessage().toLowerCase().contains("introuvable"));
    }
}
