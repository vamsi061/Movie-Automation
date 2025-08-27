package com.movielinks.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movierulz_sites")
public class Movierulz extends MovieSite {
    
    @ElementCollection
    @CollectionTable(name = "movierulz_domains", joinColumns = @JoinColumn(name = "site_id"))
    @Column(name = "domain")
    private List<String> knownDomains;
    
    @Column
    private String currentDomain;
    
    @Column
    private String mirrorSite1;
    
    @Column
    private String mirrorSite2;
    
    @Column
    private String mirrorSite3;
    
    @Column
    private Boolean hasProxy = false;
    
    @Column
    private String proxyUrl;
    
    // Constructors
    public Movierulz() {
        super();
        this.setSiteName("Movierulz");
        this.setSearchAliases(List.of(
            "movierulz", 
            "movie rulz", 
            "movierulz.com", 
            "movierulz.in", 
            "movierulz.tv",
            "movierulz.ms",
            "movierulz.pl",
            "movierulz.vpn",
            "movierulz latest",
            "movierulz new domain",
            "movierulz working link"
        ));
    }
    
    public Movierulz(String currentWorkingUrl, List<String> knownDomains) {
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
    
    public String getMirrorSite1() {
        return mirrorSite1;
    }
    
    public void setMirrorSite1(String mirrorSite1) {
        this.mirrorSite1 = mirrorSite1;
    }
    
    public String getMirrorSite2() {
        return mirrorSite2;
    }
    
    public void setMirrorSite2(String mirrorSite2) {
        this.mirrorSite2 = mirrorSite2;
    }
    
    public String getMirrorSite3() {
        return mirrorSite3;
    }
    
    public void setMirrorSite3(String mirrorSite3) {
        this.mirrorSite3 = mirrorSite3;
    }
    
    public Boolean getHasProxy() {
        return hasProxy;
    }
    
    public void setHasProxy(Boolean hasProxy) {
        this.hasProxy = hasProxy;
    }
    
    public String getProxyUrl() {
        return proxyUrl;
    }
    
    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }
    
    @Override
    public void setCurrentWorkingUrl(String currentWorkingUrl) {
        super.setCurrentWorkingUrl(currentWorkingUrl);
        this.currentDomain = extractDomain(currentWorkingUrl);
    }
}