package com.marcus.eventhub.auth;

import com.marcus.eventhub.auth.dto.AuthResponse;
import com.marcus.eventhub.auth.dto.LoginRequest;
import com.marcus.eventhub.auth.dto.RefreshTokenRequest;
import com.marcus.eventhub.auth.dto.RegisterRequest;
import com.marcus.eventhub.auth.dto.UserResponse;
import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.user.User;
import com.marcus.eventhub.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final CurrentUserService currentUserService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            AuthenticationManager authenticationManager,
            CurrentUserService currentUserService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered");
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createForUser(user);

        return AuthResponse.bearer(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validate(request.refreshToken());
        String accessToken = jwtService.generateToken(refreshToken.getUser().getEmail());
        String newRefreshToken = refreshTokenService.rotate(refreshToken);

        return AuthResponse.bearer(accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    public UserResponse me() {
        return UserResponse.from(currentUserService.getCurrentUser());
    }
}
