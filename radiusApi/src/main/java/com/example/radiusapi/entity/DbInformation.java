package com.example.radiusapi.entity;


import lombok.ToString;

@ToString
public class DbInformation {
    private String filename;
    private String filesize;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilesize() {
        return filesize;
    }

    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }
}
