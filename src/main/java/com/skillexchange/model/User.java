package com.skillexchange.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(unique = true)
    private String email;

    private String location;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String certificatePhoto; // stored as a base64 data URL

    @JsonIgnore
    private String passwordHash;

    // Skills this user can TEACH / OFFER to others
    @ElementCollection
    @CollectionTable(name = "user_skills_offered", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private List<String> skillsOffered = new ArrayList<>();

    // Skills this user WANTS to LEARN from others
    @ElementCollection
    @CollectionTable(name = "user_skills_wanted", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private List<String> skillsWanted = new ArrayList<>();

    public User() {
    }

    public User(String name, String email, String location, List<String> skillsOffered, List<String> skillsWanted) {
        this.name = name;
        this.email = email;
        this.location = location;
        this.skillsOffered = skillsOffered;
        this.skillsWanted = skillsWanted;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCertificatePhoto() {
        return certificatePhoto;
    }

    public void setCertificatePhoto(String certificatePhoto) {
        this.certificatePhoto = certificatePhoto;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<String> getSkillsOffered() {
        return skillsOffered;
    }

    public void setSkillsOffered(List<String> skillsOffered) {
        this.skillsOffered = skillsOffered;
    }

    public List<String> getSkillsWanted() {
        return skillsWanted;
    }

    public void setSkillsWanted(List<String> skillsWanted) {
        this.skillsWanted = skillsWanted;
    }
}