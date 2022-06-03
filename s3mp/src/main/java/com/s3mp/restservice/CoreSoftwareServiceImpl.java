package com.s3mp.restservice;

// CoreSoftwareServiceImpl.java
// Author: Anders Engman
// Date: 6/3/22
// Implementation of Core Software Service. Provides functionality for searching for software updates.
// This queries the db using the core software repository class

import java.util.Optional;

import com.s3mp.sqlite.CoreSoftware;

import org.springframework.stereotype.Service;

@Service
public class CoreSoftwareServiceImpl implements CoreSoftwareService {
    
    private CoreSoftwareRepository coreSoftwareRepository;

    public CoreSoftwareServiceImpl(CoreSoftwareRepository coreSoftwareRepository) {
        this.coreSoftwareRepository = coreSoftwareRepository;
    }

    @Override
    public Optional<CoreSoftware> findById(Integer id) {
        return coreSoftwareRepository.findById(id);
    }

    @Override
    public Iterable<CoreSoftware> findAll() {
        return coreSoftwareRepository.findAll();
    }
}
