package com.s3mp.restservice;

import java.util.Optional;

import com.s3mp.sqlite.CoreSoftware;

public interface CoreSoftwareService {

    Optional<CoreSoftware> findById(Integer id);

    Iterable<CoreSoftware> findAll();

}
