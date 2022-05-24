package com.s3mp.restservice;

import org.springframework.data.jpa.repository.JpaRepository;
import com.s3mp.sqlite.CoreSoftware;

public interface CoreSoftwareRepository extends JpaRepository<CoreSoftware, Integer> {
}
