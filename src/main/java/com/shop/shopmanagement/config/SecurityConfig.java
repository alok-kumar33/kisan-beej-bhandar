package com.shop.shopmanagement.config;

import com.shop.shopmanagement.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public CustomUserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC ACCESS (Login & Static Files)
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()

                        // 2. STRICTLY ADMIN ONLY (User Management) - UPDATED HERE
                        .requestMatchers("/users/**").hasAuthority("ADMIN")

                        // 3. ADMIN & OWNER ONLY (Ledger & Reports)
                        .requestMatchers("/ledger/**", "/reports/**").hasAnyAuthority("ADMIN", "OWNER")

                        // 4. STAFF, ADMIN, OWNER (Inventory, Expenses, Returns)
                        .requestMatchers("/products/**", "/expenses/**", "/returns/**").hasAnyAuthority("ADMIN", "OWNER", "STAFF")

                        // 5. GENERAL ACCESS (Sales, Dashboard, Searching)
                        .requestMatchers("/sales/**", "/invoice/**", "/").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout.permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}