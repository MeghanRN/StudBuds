package com.studbuds.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "firebase_uid", nullable = false)
    private String firebaseUid;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // New fields stored on User.
    private String major;
    private String year;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Preference preference;

    public User() {}

    // Getters and setters for all fields.
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    
    public Preference getPreference() { return preference; }
    public void setPreference(Preference preference) { this.preference = preference; }
}