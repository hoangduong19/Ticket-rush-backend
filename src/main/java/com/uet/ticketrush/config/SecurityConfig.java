package com.uet.ticketrush.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    @Qualifier("myUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    @Qualifier("adminUserDetailsService")
    private UserDetailsService adminDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private AdminJwtFilter adminJwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(customizer -> customizer.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/register",
                                "/login",
                                "/adminLogin",
                                "/admin/events/**",
                                "/users/me/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/events/**",
                                "/seats/**",
                                "/queue/**",
                                "/users/**",
                                "/admins/**"
                        )
                        .permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
//                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())

                .httpBasic(basic -> basic.disable()) // Tắt cái bảng Sign In bạn đang thấy
                .formLogin(form -> form.disable())   // Tắt trang login mặc định của Spring

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(adminJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
//                .csrf(csrf -> csrf.disable());
//
//        return http.build();
//    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
////        authProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
//        authProvider.setPasswordEncoder(new BCryptPasswordEncoder(12));
//        return authProvider;
//    }
//
//    @Bean
//    public AuthenticationProvider adminAuthenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(adminDetailsService);
////        authProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
//        authProvider.setPasswordEncoder(new BCryptPasswordEncoder(12));
//        return authProvider;
//    }
//
    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean("userAuthenticationManager")
    public AuthenticationManager userAuthenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(new BCryptPasswordEncoder(12));

        return new ProviderManager(authProvider);
    }

    @Bean("adminAuthenticationManager")
    public AuthenticationManager adminAuthenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(adminDetailsService);
        authProvider.setPasswordEncoder(new BCryptPasswordEncoder(12));

        return new ProviderManager(authProvider);
    }
}
