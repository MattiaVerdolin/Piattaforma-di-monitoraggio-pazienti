package ch.supsi.dti.homenet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity
                .formLogin(form -> form
                        .loginPage("/login")  // Custom login page
                        .loginProcessingUrl("/perform_login") // Spring Security handles login
                        .defaultSuccessUrl("/dashboard", true) // Redirect after successful login
                        .failureUrl("/login?error=true") // Redirect on failure
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/connect/**").permitAll() //consenti a garmin il push
                        .requestMatchers("/", "/login", "/perform_login").permitAll()
                        .requestMatchers("/static/**", "/img/**", "/js/**", "/css/**", "/webjars/**").permitAll() // Allow all static resources
                        .requestMatchers("/patients/create",
                                "/patients/edit/**",
                                "/patients/delete/**",
                                "/disease-categories/**",
                                "/edit-patient/**",
                                "/edit-professional/**",
                                "/healthcare-professional/delete/**",
                                "/professional/update/**",
                                "/add-professional",
                                "/healthcare-professional/**"
                        )
                        .hasAnyRole("COORD", "MED_FAM", "MED_PAL")
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}