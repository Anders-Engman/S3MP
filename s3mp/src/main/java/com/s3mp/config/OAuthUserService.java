package com.s3mp.config;

// OAuthUserService.java
// Author: Anders Engman
// Date: 6/3/22
// This service handles the OAuth token requests and validates the existence of the user in the database.

import com.s3mp.sqlite.User;
import com.s3mp.restservice.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class OAuthUserService implements UserDetailsService {

    private final UserRepository repository;

    public OAuthUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found: " + username));
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), Arrays.asList(authority));
    }
}
