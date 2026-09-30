package com.skillexchange.dto;

public class UpdateProfileRequest {

    private String bio;
    private String certificatePhoto; // base64 data URL, e.g. "data:image/png;base64,..."

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getCertificatePhoto() { return certificatePhoto; }
    public void setCertificatePhoto(String certificatePhoto) { this.certificatePhoto = certificatePhoto; }
}