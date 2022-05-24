package com.s3mp.restservice;

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
