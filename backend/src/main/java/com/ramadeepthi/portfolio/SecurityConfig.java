package com.ramadeepthi.portfolio;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
  @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception { return configuration.getAuthenticationManager(); }
  @Bean UserDetailsService userDetailsService(@Value("${ADMIN_EMAIL:}") String email, @Value("${ADMIN_PASSWORD:}") String password, PasswordEncoder encoder) {
    if (email.isBlank() || password.length() < 12) throw new IllegalStateException("Set ADMIN_EMAIL and an ADMIN_PASSWORD of at least 12 characters before starting the API.");
    return new InMemoryUserDetailsManager(User.withUsername(email).password(encoder.encode(password)).roles("ADMIN").build());
  }
  @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/events", "/api/contact").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().permitAll())
      .httpBasic(Customizer.withDefaults());
    return http.build();
  }
  @Bean CorsConfigurationSource corsConfigurationSource(@Value("${FRONTEND_ORIGIN:http://localhost:5173}") String origin) {
    CorsConfiguration c = new CorsConfiguration(); // Vite may switch from 5173 to 5174 (or another port) when the default port is busy.
    // Permit local development origins while keeping production origin explicitly configured.
    c.setAllowedOriginPatterns(List.of(origin, "http://localhost:*", "http://127.0.0.1:*"));
    c.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    c.setAllowCredentials(false);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", c); return source;
  }
}
