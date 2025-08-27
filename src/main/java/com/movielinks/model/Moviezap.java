package com.movielinks.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "moviezap_sites")
public class Moviezap extends MovieSite {
    
    @ElementCollection
    @CollectionTable(name = "moviezap_domains", joinColumns = @JoinColumn(name = "site_id"))
    @Column(name = "domain")
    private List<String> knownDomains;
    
    @Column
    private String currentDomain;
    
    @Column
    private String telegramChannel;
    
    @Column
    private String backupSite1;
    
    @Column
    private String backupSite2;
    
    @Column
    private Boolean requiresVpn = false;
    
    @Column
    private String region; // Geographic region where it works
    
    // Constructors
    public Moviezap() {
        super();
        this.setSiteName("Moviezap");
        this.setSearchAliases(List.of(
            "moviezap", 
            "movie zap", 
            "moviezap.com", 
            "moviezap.in", 
            "moviezap.org",
            "moviezap.net",
            "moviezap.co",
            "moviezap latest",
            "moviezap new domain",
            "moviezap working link",
            "moviezap telegram"
        ));
    }
    
    public Moviezap(String currentWorkingUrl, List<String> knownDomains) {
        this();
        this.setCurrentWorkingUrl(currentWorkingUrl);
        this.knownDomains = knownDomains;
        this.currentDomain = extractDomain(currentWorkingUrl);
    }
    
    // Helper method to extract domain from URL
    private String extractDomain(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            return url.replaceAll("https?://", "").split("/")[0];
        } catch (Exception e) {
            return null;
        }
    }
    
    // Getters and Setters
    public List<String> getKnownDomains() {
        return knownDomains;
    }
    
    public void setKnownDomains(List<String> knownDomains) {
        this.knownDomains = knownDomains;
    }
    
    public String getCurrentDomain() {
        return currentDomain;
    }
    
    public void setCurrentDomain(String currentDomain) {
        this.currentDomain = currentDomain;
    }
    
    public String getTelegramChannel() {
        return telegramChannel;
    }
    
    public void setTelegramChannel(String telegramChannel) {
        this.telegramChannel = telegramChannel;
    }
    
    public String getBackupSite1() {
        return backupSite1;
    }
    
    public void setBackupSite1(String backupSite1) {
        this.backupSite1 = backupSite1;
    }
    
    public String getBackupSite2() {
        return backupSite2;
    }
    
    public void setBackupSite2(String backupSite2) {
        this.backupSite2 = backupSite2;
    }
    
    public Boolean getRequiresVpn() {
        return requiresVpn;
    }
    
    public void setRequiresVpn(Boolean requiresVpn) {
        this.requiresVpn = requiresVpn;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    @Override
    public void setCurrentWorkingUrl(String currentWorkingUrl) {
        super.setCurrentWorkingUrl(currentWorkingUrl);
        this.currentDomain = extractDomain(currentWorkingUrl);
    }
}