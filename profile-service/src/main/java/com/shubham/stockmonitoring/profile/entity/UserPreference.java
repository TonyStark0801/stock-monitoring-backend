package com.shubham.stockmonitoring.profile.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences", schema = "profile")
@Data
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    
    @Column(name = "notification_enabled")
    private boolean notificationEnabled = true;
    
    @Column(name = "email_notifications")
    private boolean emailNotifications = true;
    
    @Column(name = "sms_notifications")
    private boolean smsNotifications = false;
    
    @Column(name = "push_notifications")
    private boolean pushNotifications = true;
    
    @Column(name = "default_currency")
    private String defaultCurrency = "USD";
    
    @Column(name = "timezone")
    private String timezone = "UTC";
    
    @Column(name = "language")
    private String language = "en";
    
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
