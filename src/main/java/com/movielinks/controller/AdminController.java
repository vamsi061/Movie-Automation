package com.movielinks.controller;

import com.movielinks.model.MovieSite;
import com.movielinks.repository.MovieSiteRepository;
import com.movielinks.service.MonitoringService;
import com.movielinks.service.MovieSiteSearchService;
import com.movielinks.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private MovieSiteRepository movieSiteRepository;
    
    @Autowired
    private MonitoringService monitoringService;
    
    @Autowired
    private MovieSiteSearchService searchService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Get admin dashboard overview
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            MonitoringService.SystemHealthStatus healthStatus = monitoringService.getSystemHealth();
            Map<String, Object> statistics = monitoringService.getSiteStatistics();
            List<MovieSite> recentlyUpdated = movieSiteRepository.findRecentlyUpdated(
                LocalDateTime.now().minusHours(24)
            );
            
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("healthStatus", healthStatus);
            dashboard.put("statistics", statistics);
            dashboard.put("recentActivity", recentlyUpdated);
            dashboard.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            logger.error("Error getting dashboard data: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load dashboard"));
        }
    }
    
    /**
     * Get all movie sites with pagination
     */
    @GetMapping("/sites")
    public ResponseEntity<Map<String, Object>> getAllSites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String search) {
        
        try {
            List<MovieSite> allSites = movieSiteRepository.findAll();
            
            // Filter by status if provided
            if (!status.isEmpty()) {
                allSites = allSites.stream()
                    .filter(site -> site.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
            }
            
            // Filter by search term if provided
            if (!search.isEmpty()) {
                allSites = allSites.stream()
                    .filter(site -> site.getSiteName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            // Sort by last updated
            allSites.sort((a, b) -> {
                if (a.getLastUpdated() == null) return 1;
                if (b.getLastUpdated() == null) return -1;
                return b.getLastUpdated().compareTo(a.getLastUpdated());
            });
            
            // Pagination
            int totalElements = allSites.size();
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalElements);
            
            List<MovieSite> paginatedSites = fromIndex < totalElements ? 
                allSites.subList(fromIndex, toIndex) : new ArrayList<>();
            
            Map<String, Object> response = Map.of(
                "sites", paginatedSites,
                "totalElements", totalElements,
                "totalPages", (int) Math.ceil((double) totalElements / size),
                "currentPage", page,
                "pageSize", size
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting sites: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load sites"));
        }
    }
    
    /**
     * Get specific site details
     */
    @GetMapping("/sites/{id}")
    public ResponseEntity<Map<String, Object>> getSiteDetails(@PathVariable Long id) {
        try {
            Optional<MovieSite> siteOpt = movieSiteRepository.findById(id);
            
            if (siteOpt.isPresent()) {
                return ResponseEntity.ok(Map.of("site", siteOpt.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error getting site details: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load site details"));
        }
    }
    
    /**
     * Update site manually
     */
    @PutMapping("/sites/{id}")
    public ResponseEntity<Map<String, Object>> updateSite(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            Optional<MovieSite> siteOpt = movieSiteRepository.findById(id);
            
            if (siteOpt.isPresent()) {
                MovieSite site = siteOpt.get();
                
                if (updates.containsKey("currentWorkingUrl")) {
                    site.setCurrentWorkingUrl((String) updates.get("currentWorkingUrl"));
                }
                
                if (updates.containsKey("status")) {
                    site.setStatus((String) updates.get("status"));
                }
                
                if (updates.containsKey("isActive")) {
                    site.setIsActive((Boolean) updates.get("isActive"));
                }
                
                if (updates.containsKey("notes")) {
                    site.setNotes((String) updates.get("notes"));
                }
                
                site.setLastUpdated(LocalDateTime.now());
                movieSiteRepository.save(site);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Site updated successfully",
                    "site", site
                ));
                
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error updating site: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update site"));
        }
    }
    
    /**
     * Delete site
     */
    @DeleteMapping("/sites/{id}")
    public ResponseEntity<Map<String, Object>> deleteSite(@PathVariable Long id) {
        try {
            if (movieSiteRepository.existsById(id)) {
                movieSiteRepository.deleteById(id);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Site deleted successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error deleting site: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete site"));
        }
    }
    
    /**
     * Force refresh specific site
     */
    @PostMapping("/sites/{id}/refresh")
    public ResponseEntity<Map<String, Object>> refreshSite(@PathVariable Long id) {
        try {
            Optional<MovieSite> siteOpt = movieSiteRepository.findById(id);
            
            if (siteOpt.isPresent()) {
                MovieSite site = siteOpt.get();
                
                // Perform fresh search
                MovieSite updatedSite = searchService.findWorkingLink(site.getSiteName());
                updatedSite.setId(id);
                movieSiteRepository.save(updatedSite);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Site refreshed successfully",
                    "site", updatedSite
                ));
                
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error refreshing site: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to refresh site"));
        }
    }
    
    /**
     * Force refresh all sites
     */
    @PostMapping("/sites/refresh-all")
    public ResponseEntity<Map<String, Object>> refreshAllSites() {
        try {
            List<MovieSite> allSites = movieSiteRepository.findAll();
            List<String> siteNames = allSites.stream()
                .map(MovieSite::getSiteName)
                .collect(Collectors.toList());
            
            // Trigger background refresh
            new Thread(() -> {
                try {
                    searchService.findWorkingLinks(siteNames);
                } catch (Exception e) {
                    logger.error("Error in background refresh: {}", e.getMessage());
                }
            }).start();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Refresh started for all sites",
                "sitesCount", siteNames.size()
            ));
            
        } catch (Exception e) {
            logger.error("Error starting refresh all: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to start refresh"));
        }
    }
    
    /**
     * Get monitoring statistics
     */
    @GetMapping("/monitoring/stats")
    public ResponseEntity<Map<String, Object>> getMonitoringStats() {
        try {
            MonitoringService.SystemHealthStatus healthStatus = monitoringService.getSystemHealth();
            Map<String, Object> statistics = monitoringService.getSiteStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("healthStatus", healthStatus);
            response.put("statistics", statistics);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting monitoring stats: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load monitoring stats"));
        }
    }
    
    /**
     * Trigger manual health check
     */
    @PostMapping("/monitoring/health-check")
    public ResponseEntity<Map<String, Object>> triggerHealthCheck() {
        try {
            // Run health check in background
            new Thread(() -> {
                try {
                    monitoringService.performScheduledHealthCheck();
                } catch (Exception e) {
                    logger.error("Error in manual health check: {}", e.getMessage());
                }
            }).start();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Health check started"
            ));
            
        } catch (Exception e) {
            logger.error("Error starting health check: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to start health check"));
        }
    }
    
    /**
     * Send test notification
     */
    @PostMapping("/notifications/test")
    public ResponseEntity<Map<String, Object>> sendTestNotification() {
        try {
            notificationService.sendTestNotification();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Test notification sent"
            ));
            
        } catch (Exception e) {
            logger.error("Error sending test notification: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send test notification"));
        }
    }
    
    /**
     * Get system configuration
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSystemConfig() {
        try {
            Map<String, Object> config = Map.of(
                "supportedSites", Arrays.asList(
                    "movierulz", "moviezap", "tamilrockers", "filmywap",
                    "worldfree4u", "9xmovies", "khatrimaza", "bolly4u"
                ),
                "monitoringEnabled", true,
                "notificationsEnabled", true,
                "autoRefreshInterval", "6 hours",
                "maxSearchResults", 10
            );
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            logger.error("Error getting system config: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load system config"));
        }
    }
    
    /**
     * Get activity logs (simplified)
     */
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            // Get recent sites activity as logs
            List<MovieSite> recentActivity = movieSiteRepository.findRecentlyUpdated(
                LocalDateTime.now().minusDays(7)
            );
            
            // Convert to log format
            List<Map<String, Object>> logs = recentActivity.stream()
                .map(site -> Map.of(
                    "timestamp", site.getLastUpdated(),
                    "action", "Site Updated",
                    "siteName", site.getSiteName(),
                    "status", site.getStatus(),
                    "url", site.getCurrentWorkingUrl() != null ? site.getCurrentWorkingUrl() : "N/A"
                ))
                .sorted((a, b) -> ((LocalDateTime) b.get("timestamp")).compareTo((LocalDateTime) a.get("timestamp")))
                .collect(Collectors.toList());
            
            // Pagination
            int totalElements = logs.size();
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalElements);
            
            List<Map<String, Object>> paginatedLogs = fromIndex < totalElements ? 
                logs.subList(fromIndex, toIndex) : new ArrayList<>();
            
            Map<String, Object> response = Map.of(
                "logs", paginatedLogs,
                "totalElements", totalElements,
                "totalPages", (int) Math.ceil((double) totalElements / size),
                "currentPage", page,
                "pageSize", size
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting activity logs: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load activity logs"));
        }
    }
    
    /**
     * Add new site to monitor
     */
    @PostMapping("/sites")
    public ResponseEntity<Map<String, Object>> addNewSite(@RequestBody Map<String, Object> siteData) {
        try {
            String siteName = (String) siteData.get("siteName");
            
            if (siteName == null || siteName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Site name is required"));
            }
            
            // Check if site already exists
            Optional<MovieSite> existingSite = movieSiteRepository.findBySiteNameIgnoreCase(siteName);
            if (existingSite.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Site already exists"));
            }
            
            // Search for the new site
            MovieSite newSite = searchService.findWorkingLink(siteName);
            movieSiteRepository.save(newSite);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Site added successfully",
                "site", newSite
            ));
            
        } catch (Exception e) {
            logger.error("Error adding new site: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to add new site"));
        }
    }
}