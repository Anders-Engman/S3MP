package com.s3mp.sqlite;

// User.java
// Author: Anders Engman
// Date: 6/3/22
// Entity class pertaining to the User Table.

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String role;

    public Integer getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }

    public String getRole() {
        return this.role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
