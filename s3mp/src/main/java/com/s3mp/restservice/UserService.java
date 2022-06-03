package com.s3mp.restservice;

// UserService.java
// Author: Anders Engman
// Date: 6/3/22
// the interface for the userService. See UserServiceImpl.java for the implementation.

import java.util.Optional;

import com.s3mp.sqlite.User;

public interface UserService {

    Optional<User> findById(Integer id);

    Iterable<User> findAll();
}
