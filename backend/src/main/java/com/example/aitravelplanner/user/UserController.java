package com.example.aitravelplanner.user;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // User registration is now handled by the frontend with Supabase.
    // Endpoints for managing user-specific data can be added here later.
}
