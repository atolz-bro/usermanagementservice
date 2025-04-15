package com.test.usermanagementservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.usermanagementservice.controllers.UserManagementController;
import com.test.usermanagementservice.filters.JwtAuthenticationFilter;
import com.test.usermanagementservice.models.AppUser;
import com.test.usermanagementservice.service.CustomUserDetailsService;
import com.test.usermanagementservice.service.UserRepository;
import com.test.usermanagementservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserManagementController.class)
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    HttpServletRequest httpServletRequest;
    @MockBean
    HttpServletResponse  httpServletResponse;


    @Autowired
    private ObjectMapper objectMapper;

    private AppUser sampleUser;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        sampleUser = new AppUser();
        sampleUser.setId(1L);
        sampleUser.setUsername("john");
        sampleUser.setEmail("john@example.com");
        sampleUser.setPassword("password");
        sampleUser.setRole("USER");
        sampleUser.setCreatedAt(Timestamp.from(Instant.now()));

        jwtToken =  jwtUtil.generateToken(sampleUser.getUsername(),sampleUser.getRole());
    }

    @Test
    @WithMockUser
    void testCreateUser_Success() throws Exception {
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenReturn(sampleUser);
        when(userDetailsService.loadUserByUsername(sampleUser.getUsername()))
                .thenReturn(new User(sampleUser.getUsername(),sampleUser.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + sampleUser.getRole()))));
        when(jwtUtil.validateToken(jwtToken, sampleUser.getUsername())).thenReturn(true);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));

        verify(userRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    @WithMockUser
    void testCreateUser_InvalidInput() throws Exception {
        AppUser invalidUser = new AppUser(); // Missing required fields

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    @WithMockUser
    void testGetUser_Found() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    @WithMockUser
    void testGetUser_NotFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateUser_Found() throws Exception {
        AppUser updatedUser = new AppUser();
        updatedUser.setUsername("newjohn");
        updatedUser.setEmail("new@example.com");
        updatedUser.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(AppUser.class))).thenReturn(sampleUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newjohn"));
    }

    @Test
    @WithMockUser
    void testUpdateUser_NotFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteUser_Found() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User Deleted"));
    }

    @Test
    @WithMockUser
    void testDeleteUser_NotFound() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/1"))

                .andExpect(status().isNotFound());
    }
}
