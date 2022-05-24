package com.s3mp.restservice;

import java.util.Optional;

import com.s3mp.sqlite.User;

public interface UserService {

    Optional<User> findById(Integer id);

    Iterable<User> findAll();
}
