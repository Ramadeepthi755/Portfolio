package com.ramadeepthi.portfolio;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthenticationManager authenticationManager;

  public AuthController(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody LoginRequest request) {
    if (request == null || request.email() == null || request.password() == null
        || request.email().isBlank() || request.password().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required.");
    }
    try {
      Authentication result = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));
      return Map.of("authenticated", result.isAuthenticated(), "email", result.getName());
    } catch (BadCredentialsException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect admin email or password.");
    }
  }

  public record LoginRequest(String email, String password) {}
}
