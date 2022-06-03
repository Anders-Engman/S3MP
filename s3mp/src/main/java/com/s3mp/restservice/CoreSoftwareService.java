package com.s3mp.restservice;

// CoreSoftwareService.java
// Author: Anders Engman
// Date: 6/3/22
// the interface for the coreSoftwareService. See CoreSoftwareServiceImpl.java for the implementation.

import java.util.Optional;

import com.s3mp.sqlite.CoreSoftware;

public interface CoreSoftwareService {

    Optional<CoreSoftware> findById(Integer id);

    Iterable<CoreSoftware> findAll();

}
