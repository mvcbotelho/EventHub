package com.marcus.eventhub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcus.eventhub.auth.dto.LoginRequest;
import com.marcus.eventhub.auth.dto.RegisterRequest;
import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.user.User;
import com.marcus.eventhub.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("Marcus", "dup@test.com", "123456");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void registerShouldPersistEncodedPassword() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterRequest request = new RegisterRequest("Marcus", "new@test.com", "123456");

        var response = authService.register(request);

        assertThat(response.email()).isEqualTo("new@test.com");
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginShouldReturnAccessAndRefreshTokens() {
        User user = new User("Marcus", "user@test.com", "hash");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("user@test.com")).thenReturn("jwt-token");
        when(refreshTokenService.createForUser(user)).thenReturn("refresh-token");

        LoginRequest request = new LoginRequest("user@test.com", "123456");
        var response = authService.login(request);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("user@test.com", "123456")
        );
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.type()).isEqualTo("Bearer");
    }
}
