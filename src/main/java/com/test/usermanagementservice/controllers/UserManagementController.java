package com.test.usermanagementservice.controllers;

import com.test.usermanagementservice.models.AppUser;
import com.test.usermanagementservice.service.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Tag(name = "User Management Controller", description = "Endpoints for user apis\n The apis requires Authorization header and jwt token of an already authenticated user(e.g admin)")
@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    @Autowired
    private final UserRepository userRepository;
    public final BCryptPasswordEncoder passwordEncoder;

    public UserManagementController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Create User
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user with encoded password and returns the saved user object",
            security = @SecurityRequirement(name = "bearerAuth")

    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AppUser.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody AppUser user) {
        if(user.getUsername() == null || user.getPassword() == null || user.getEmail() == null){
            return ResponseEntity.badRequest().build();
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(Timestamp.from(Instant.now()));
        return ResponseEntity.ok(userRepository.save(user));
       
    }

    // Retrieve User
    @Operation(
            summary = "Retrieve user by ID",
            description = "Fetches a user's details based on the provided user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AppUser.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getUser(@PathVariable Long id) {
        Optional<AppUser> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update User
    @Operation(
            summary = "Update an existing user",
            description = "Updates the user information for the given user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AppUser.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody AppUser updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setEmail(updatedUser.getEmail());
                    user.setRole(updatedUser.getRole());
                    user.setUpdatedAt(Timestamp.from(Instant.now()));
                    userRepository.save(user);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete User
    @Operation(
            summary = "Delete a user by ID",
            description = "Deletes a user from the system based on the provided ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully deleted",
            content = @Content(
            mediaType = "application/text",
                    schema = @Schema(implementation = String.class)
            )),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User Deleted");
        }
        return ResponseEntity.notFound().build();
    }
}

