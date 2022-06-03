package com.s3mp.restservice;

// CoreSoftwareRepository.java
// Author: Anders Engman
// Date: 6/3/22
// This class enables the application to query the CoreSoftware table of the db.

import org.springframework.data.jpa.repository.JpaRepository;
import com.s3mp.sqlite.CoreSoftware;

public interface CoreSoftwareRepository extends JpaRepository<CoreSoftware, Integer> {
}
