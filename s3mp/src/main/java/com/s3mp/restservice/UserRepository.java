package com.s3mp.restservice;


// UserRepository.java
// Author: Anders Engman
// Date: 6/3/22
// This class enables the application to query the user table of the db.
// An additional function was added to enable querying by username.

import org.springframework.data.jpa.repository.JpaRepository;
import com.s3mp.sqlite.User;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}