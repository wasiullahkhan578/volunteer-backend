package com.community.volunteer_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC ENDPOINTS
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. ADMIN ENDPOINTS
                        .requestMatchers("/api/users/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 3. ORGANIZER ENDPOINTS (The "Action" Layer)
                        .requestMatchers("/api/organizer/**").hasRole("ORGANIZER")
                        .requestMatchers("/api/events/organizer/**").hasRole("ORGANIZER")
                        .requestMatchers("/api/events/participation/**").hasRole("ORGANIZER")

                        // 4. VOLUNTEER ENDPOINTS (The "Discovery" Layer)
                        // Explicitly defining the POST method for joining missions
                        .requestMatchers("/api/events/join/**").hasAuthority("ROLE_VOLUNTEER")
                        .requestMatchers("/api/volunteer/**").hasRole("VOLUNTEER")
                        .requestMatchers("/api/events/history").hasRole("VOLUNTEER")
                        .requestMatchers("/api/events/cancel/**").hasRole("VOLUNTEER")

                        // 5. SHARED ENDPOINTS
                        .requestMatchers("/api/events/all").authenticated()
                        .requestMatchers("/api/events/{id}").authenticated()
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/users/update").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/feedback/**").authenticated()

                        // 6. PROTECT EVERYTHING ELSE
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}