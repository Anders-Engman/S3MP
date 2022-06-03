package com.s3mp.restservice;

// UserServiceImpl.java
// Author: Anders Engman
// Date: 6/3/22
// Implementation of User Service. Provides functionality for searching for users.
// This queries the db using the user repository class

import java.util.Optional;
import com.s3mp.sqlite.User;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }
    
}
