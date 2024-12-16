package org.esupportail.scim.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableConfigurationProperties
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
@EnableAsync
public class WebSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);


    @Value("${scim.username}") String scimUsername;

    @Value("${scim.password}") String scimPassword;

    @Bean
    public SecurityFilterChain filterChainScim(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .authenticationProvider(scimBasicAuthenticationProvider());
        // csrf disabled for API SCIM
        http.csrf().disable();
        return http.build();
    }

    @Bean
    public AuthenticationProvider scimBasicAuthenticationProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
                if (token.getName().equals(scimUsername) && token.getCredentials().equals(scimPassword)) {
                    return new UsernamePasswordAuthenticationToken(scimUsername, scimPassword, List.of());
                }
                return null;
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

}
