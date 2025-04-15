package com.esiitech.publication_memoire.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String uploadDir;
    private String pdfDir;
    private String sofficePath;
    private String basePath;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getPdfDir() {
        return pdfDir;
    }

    public void setPdfDir(String pdfDir) {
        this.pdfDir = pdfDir;
    }

    public String getSofficePath() {
        return sofficePath;
    }

    public void setSofficePath(String sofficePath) {
        this.sofficePath = sofficePath;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
