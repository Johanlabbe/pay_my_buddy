package com.example.myapp.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.myapp.entity.User;
import com.example.myapp.repository.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Chargement de l’utilisateur par son email.
     * En cas d’absence, lèvera UsernameNotFoundException → 401 Unauthorized.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        String role = Optional.ofNullable(entity.getRole())
                .filter(r -> !r.isBlank())
                .orElse("USER");

        return org.springframework.security.core.userdetails.User
                .withUsername(entity.getEmail())
                .password(entity.getPassword())
                .roles(role)
                .build();
    }

    // public static void main(String[] args) {
    //     System.out.println(matches("pass", "$2a$10$uTzdCeIhJJohPRUkt6JUAOM.xIm2uF7Nr/97v0yYvkRpeJ.RP6O/u"));
    // }

    // static private Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2(a|y|b)?\\$(\\d\\d)\\$[./0-9A-Za-z]{53}");

    // static public boolean matches(CharSequence rawPassword, String encodedPassword) {
    //     if (rawPassword == null) {
    //         throw new IllegalArgumentException("rawPassword cannot be null");
    //     }
    //     if (encodedPassword == null || encodedPassword.length() == 0) {
    //         // this.logger.warn("Empty encoded password");
    //         return false;
    //     }
    //     if (!BCRYPT_PATTERN.matcher(encodedPassword).matches()) {
    //         // this.logger.warn("Encoded password does not look like BCrypt");
    //         return false;
    //     }
    //     return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    // }
}
