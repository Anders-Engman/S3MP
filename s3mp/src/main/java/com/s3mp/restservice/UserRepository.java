package com.s3mp.restservice;

import org.springframework.data.jpa.repository.JpaRepository;
import com.s3mp.sqlite.User;

public interface UserRepository extends JpaRepository<User, Integer> {
}