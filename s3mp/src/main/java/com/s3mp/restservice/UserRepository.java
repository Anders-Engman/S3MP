package com.s3mp.restservice;

import org.springframework.data.jpa.repository.JpaRepository;
import com.s3mp.sqlite.User;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}