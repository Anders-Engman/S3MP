package com.s3mp.sqlite;

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

    private Boolean isExperimental;

    public Integer getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }

    public Boolean getExperimentalStatus() {
        return this.isExperimental;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setExperimentalStatus(Boolean isExperimental) {
        this.isExperimental = isExperimental;
    }
}
