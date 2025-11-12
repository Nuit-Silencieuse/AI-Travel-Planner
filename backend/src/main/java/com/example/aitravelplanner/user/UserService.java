package com.example.aitravelplanner.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // User creation is now handled by Supabase.
    // Business logic related to users can be added here later.
}
