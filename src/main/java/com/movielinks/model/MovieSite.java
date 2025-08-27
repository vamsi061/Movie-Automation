package com.movielinks.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movie_sites")
public class MovieSite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String siteName;
    
    @Column(nullable = false)
    private String currentWorkingUrl;
    
    @ElementCollection
    @CollectionTable(name = "site_aliases", joinColumns = @JoinColumn(name = "site_id"))
    @Column(name = "alias")
    private List<String> searchAliases;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime lastChecked;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column
    private String status; // WORKING, DOWN, REDIRECTED, BLOCKED
    
    @Column
    private Integer responseTime; // in milliseconds
    
    @Column(length = 1000)
    private String notes;
    
    // Constructors
    public MovieSite() {}
    
    public MovieSite(String siteName, String currentWorkingUrl, List<String> searchAliases) {
        this.siteName = siteName;
        this.currentWorkingUrl = currentWorkingUrl;
        this.searchAliases = searchAliases;
        this.lastChecked = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.status = "UNKNOWN";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSiteName() {
        return siteName;
    }
    
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
    
    public String getCurrentWorkingUrl() {
        return currentWorkingUrl;
    }
    
    public void setCurrentWorkingUrl(String currentWorkingUrl) {
        this.currentWorkingUrl = currentWorkingUrl;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public List<String> getSearchAliases() {
        return searchAliases;
    }
    
    public void setSearchAliases(List<String> searchAliases) {
        this.searchAliases = searchAliases;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getLastChecked() {
        return lastChecked;
    }
    
    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(Integer responseTime) {
        this.responseTime = responseTime;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}