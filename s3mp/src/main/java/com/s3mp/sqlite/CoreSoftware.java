package com.s3mp.sqlite;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class CoreSoftware {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Float versionNumber;

    private Boolean stable;

    private Float fileSize;

    public Integer getId() {
        return this.id;
    }

    public Float getVersionNumber() {
        return this.versionNumber;
    }

    public Boolean getStability() {
        return this.stable;
    }

    public Float getFileSize() {
        return this.fileSize;
    }

    public void setVersionNumber(Float versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setStability(Boolean stable) {
        this.stable = stable;
    }
    
    public void setFileSize(Float fileSize) {
        this.fileSize = fileSize;
    }
}
