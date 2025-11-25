package com.projects.My_Instagram.controllers;

import com.projects.My_Instagram.DTOs.request.LoginRequest;
import com.projects.My_Instagram.DTOs.request.UserRequest;
import com.projects.My_Instagram.DTOs.response.AuthResponse;
import com.projects.My_Instagram.DTOs.response.SignUpResponse;
import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.constants.exception.ExceptionMessages;
import com.projects.My_Instagram.exceptions.InvalidCredentialsException;
import com.projects.My_Instagram.jwt.JwtUtil;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;
import static com.projects.My_Instagram.constants.response.ResponseMessages.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@RequestBody UserRequest userRequest){
        UserResponse user = userService.createUser(userRequest);
        SignUpResponse signUpResponse = new SignUpResponse(USER_CREATED_SUCCESSFULLY.getMessage(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(signUpResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtUtil.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(new AuthResponse(token));
    }
}

