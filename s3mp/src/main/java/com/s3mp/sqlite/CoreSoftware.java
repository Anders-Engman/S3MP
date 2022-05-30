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

    private Double versionNumber;

    private Boolean stable;

    private Double fileSize;

    private String contents;

    public Integer getId() {
        return this.id;
    }

    public Double getVersionNumber() {
        return this.versionNumber;
    }

    public Boolean getStability() {
        return this.stable;
    }

    public Double getFileSize() {
        return this.fileSize;
    }

    public String getContents() {
        return this.contents;
    }

    public void setVersionNumber(Double versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setStability(Boolean stable) {
        this.stable = stable;
    }
    
    public void setFileSize(Double fileSize) {
        this.fileSize = fileSize;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
