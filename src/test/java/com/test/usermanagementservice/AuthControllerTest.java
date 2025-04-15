package com.test.usermanagementservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.usermanagementservice.DTOs.AuthRequest;
import com.test.usermanagementservice.DTOs.AuthResponse;
import com.test.usermanagementservice.controllers.AuthController;
import com.test.usermanagementservice.filters.JwtAuthenticationFilter;
import com.test.usermanagementservice.service.CustomUserDetailsService;
import com.test.usermanagementservice.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CommandLineRunner commandLineRunner;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_shouldReturnTokenOnValidCredentials() throws Exception {
        AuthRequest authRequest = new AuthRequest("admin", "admin123");
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);

        Mockito.when(jwtUtil.generateToken("testuser", "ROLE_USER"))
                .thenReturn("mock-jwt-token");

        System.out.println();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void login_shouldReturnUnauthorizedOnInvalidCredentials() throws Exception {
        AuthRequest authRequest = new AuthRequest("testuser", "wrongpassword");
/*
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));*/

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }
}
