package com.horizonbank.homebanking.identity.web;

import com.horizonbank.homebanking.identity.application.AuthenticationService;
import com.horizonbank.homebanking.identity.application.RegisterUserService;
import com.horizonbank.homebanking.identity.application.RegisteredUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserService registerUserService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisteredUser registered = registerUserService.register(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(registered.userId(), registered.email()));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.from(authenticationService.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return TokenResponse.from(authenticationService.refresh(request.refreshToken()));
    }
}