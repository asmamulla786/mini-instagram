package com.projects.My_Instagram.controllers;

import com.projects.My_Instagram.DTOs.request.LoginRequest;
import com.projects.My_Instagram.DTOs.request.UserRequest;
import com.projects.My_Instagram.DTOs.response.AuthResponse;
import com.projects.My_Instagram.DTOs.response.SignUpResponse;
import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.helper.UserHelper;
import com.projects.My_Instagram.jwt.JwtUtil;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.projects.My_Instagram.constants.response.ResponseMessages.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
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
        User user = userService.findUserByUserName(userDetails.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication){
        String username = authentication.getName();
        User user = userService.findUserByUserName(username);

        return ResponseEntity.ok(UserHelper.formUserResponse(user));
    }
}

