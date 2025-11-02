package com.shubham.stockmonitoring.profile.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles", schema = "profile")
@Data
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    private String address;
    private String city;
    private String country;
    
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;
    
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
