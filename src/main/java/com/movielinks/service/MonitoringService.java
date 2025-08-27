package com.movielinks.service;

import com.movielinks.model.MovieSite;
import com.movielinks.repository.MovieSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);
    
    @Autowired
    private MovieSiteRepository movieSiteRepository;
    
    @Autowired
    private MovieSiteSearchService searchService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${monitoring.check-interval-hours:6}")
    private int checkIntervalHours;
    
    @Value("${monitoring.alert-threshold-minutes:30}")
    private int alertThresholdMinutes;
    
    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;
    
    /**
     * Scheduled health check for all movie sites
     * Runs every 6 hours by default
     */
    @Scheduled(fixedRateString = "${monitoring.check-interval-ms:21600000}") // 6 hours
    public void performScheduledHealthCheck() {
        logger.info("Starting scheduled health check for all movie sites");
        
        try {
            List<String> sitesToCheck = Arrays.asList(
                "movierulz", "moviezap", "tamilrockers", "filmywap",
                "worldfree4u", "9xmovies", "khatrimaza", "bolly4u"
            );
            
            Map<String, String> results = new HashMap<>();
            List<String> downSites = new ArrayList<>();
            List<String> newWorkingSites = new ArrayList<>();
            
            for (String siteName : sitesToCheck) {
                try {
                    MovieSite previousState = movieSiteRepository.findBySiteNameIgnoreCase(siteName).orElse(null);
                    MovieSite currentState = searchService.findWorkingLink(siteName);
                    
                    // Save current state
                    if (previousState != null) {
                        currentState.setId(previousState.getId());
                    }
                    movieSiteRepository.save(currentState);
                    
                    results.put(siteName, currentState.getStatus());
                    
                    // Check for status changes
                    if (previousState != null) {
                        if ("WORKING".equals(previousState.getStatus()) && !"WORKING".equals(currentState.getStatus())) {
                            downSites.add(siteName);
                        } else if (!"WORKING".equals(previousState.getStatus()) && "WORKING".equals(currentState.getStatus())) {
                            newWorkingSites.add(siteName);
                        }
                    }
                    
                    // Add delay between checks
                    Thread.sleep(3000);
                    
                } catch (Exception e) {
                    logger.error("Error checking site {}: {}", siteName, e.getMessage());
                    results.put(siteName, "ERROR");
                }
            }
            
            // Generate monitoring report
            MonitoringReport report = generateMonitoringReport(results, downSites, newWorkingSites);
            
            // Send alerts if needed
            if (!downSites.isEmpty() || !newWorkingSites.isEmpty()) {
                sendAlerts(report);
            }
            
            // Send report to n8n
            sendReportToN8n(report);
            
            logger.info("Scheduled health check completed. Working: {}, Down: {}", 
                       report.getWorkingSitesCount(), report.getDownSitesCount());
            
        } catch (Exception e) {
            logger.error("Error during scheduled health check: {}", e.getMessage());
        }
    }
    
    /**
     * Check specific sites that haven't been checked recently
     */
    @Scheduled(fixedRateString = "${monitoring.stale-check-ms:3600000}") // 1 hour
    public void checkStaleSites() {
        LocalDateTime staleThreshold = LocalDateTime.now().minusHours(checkIntervalHours);
        List<MovieSite> staleSites = movieSiteRepository.findSitesNeedingCheck(staleThreshold);
        
        if (!staleSites.isEmpty()) {
            logger.info("Found {} stale sites that need checking", staleSites.size());
            
            for (MovieSite staleSite : staleSites) {
                try {
                    MovieSite updatedSite = searchService.findWorkingLink(staleSite.getSiteName());
                    updatedSite.setId(staleSite.getId());
                    movieSiteRepository.save(updatedSite);
                    
                    Thread.sleep(2000);
                } catch (Exception e) {
                    logger.error("Error checking stale site {}: {}", staleSite.getSiteName(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Generate comprehensive monitoring report
     */
    public MonitoringReport generateMonitoringReport(Map<String, String> currentResults, 
                                                   List<String> downSites, 
                                                   List<String> newWorkingSites) {
        
        List<MovieSite> allSites = movieSiteRepository.findAll();
        
        long workingCount = currentResults.values().stream()
            .filter(status -> "WORKING".equals(status))
            .count();
        
        long downCount = currentResults.values().stream()
            .filter(status -> !"WORKING".equals(status))
            .count();
        
        // Calculate uptime percentage
        double uptimePercentage = allSites.isEmpty() ? 0.0 : 
            (double) workingCount / currentResults.size() * 100;
        
        // Get response time statistics
        OptionalDouble avgResponseTime = allSites.stream()
            .filter(site -> site.getResponseTime() != null)
            .mapToInt(MovieSite::getResponseTime)
            .average();
        
        return MonitoringReport.builder()
            .timestamp(LocalDateTime.now())
            .totalSites(currentResults.size())
            .workingSitesCount((int) workingCount)
            .downSitesCount((int) downCount)
            .uptimePercentage(uptimePercentage)
            .averageResponseTime(avgResponseTime.orElse(0.0))
            .siteStatuses(currentResults)
            .downSites(downSites)
            .newWorkingSites(newWorkingSites)
            .build();
    }
    
    /**
     * Send alerts for site status changes
     */
    private void sendAlerts(MonitoringReport report) {
        try {
            if (!report.getDownSites().isEmpty()) {
                String alertMessage = String.format(
                    "🚨 ALERT: %d movie sites are now DOWN: %s",
                    report.getDownSites().size(),
                    String.join(", ", report.getDownSites())
                );
                notificationService.sendAlert(alertMessage, "CRITICAL");
            }
            
            if (!report.getNewWorkingSites().isEmpty()) {
                String successMessage = String.format(
                    "✅ GOOD NEWS: %d movie sites are now WORKING: %s",
                    report.getNewWorkingSites().size(),
                    String.join(", ", report.getNewWorkingSites())
                );
                notificationService.sendAlert(successMessage, "INFO");
            }
            
        } catch (Exception e) {
            logger.error("Error sending alerts: {}", e.getMessage());
        }
    }
    
    /**
     * Send monitoring report to n8n for further processing
     */
    private void sendReportToN8n(MonitoringReport report) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> payload = Map.of(
                "action", "monitoring_report",
                "report", report,
                "timestamp", LocalDateTime.now().toString()
            );
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            String webhookUrl = n8nWebhookUrl + "/webhook/monitoring";
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("Monitoring report sent to n8n successfully");
            }
            
        } catch (Exception e) {
            logger.warn("Failed to send monitoring report to n8n: {}", e.getMessage());
        }
    }
    
    /**
     * Get current system health status
     */
    public SystemHealthStatus getSystemHealth() {
        List<MovieSite> allSites = movieSiteRepository.findAll();
        List<MovieSite> workingSites = movieSiteRepository.findWorkingSites();
        
        long totalSites = allSites.size();
        long workingCount = workingSites.size();
        double uptimePercentage = totalSites == 0 ? 0.0 : (double) workingCount / totalSites * 100;
        
        // Calculate average response time
        OptionalDouble avgResponseTime = allSites.stream()
            .filter(site -> site.getResponseTime() != null)
            .mapToInt(MovieSite::getResponseTime)
            .average();
        
        // Determine overall health status
        String healthStatus;
        if (uptimePercentage >= 80) {
            healthStatus = "HEALTHY";
        } else if (uptimePercentage >= 50) {
            healthStatus = "DEGRADED";
        } else {
            healthStatus = "CRITICAL";
        }
        
        return SystemHealthStatus.builder()
            .status(healthStatus)
            .totalSites((int) totalSites)
            .workingSites((int) workingCount)
            .downSites((int) (totalSites - workingCount))
            .uptimePercentage(uptimePercentage)
            .averageResponseTime(avgResponseTime.orElse(0.0))
            .lastChecked(LocalDateTime.now())
            .build();
    }
    
    /**
     * Get detailed site statistics
     */
    public Map<String, Object> getSiteStatistics() {
        List<Object[]> statusCounts = movieSiteRepository.countByStatus();
        List<MovieSite> recentlyUpdated = movieSiteRepository.findRecentlyUpdated(
            LocalDateTime.now().minusHours(24)
        );
        
        Map<String, Long> statusDistribution = statusCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        
        return Map.of(
            "statusDistribution", statusDistribution,
            "recentlyUpdatedCount", recentlyUpdated.size(),
            "totalSitesTracked", movieSiteRepository.count(),
            "lastUpdateTime", LocalDateTime.now()
        );
    }
    
    // Inner classes for data transfer
    public static class MonitoringReport {
        private LocalDateTime timestamp;
        private int totalSites;
        private int workingSitesCount;
        private int downSitesCount;
        private double uptimePercentage;
        private double averageResponseTime;
        private Map<String, String> siteStatuses;
        private List<String> downSites;
        private List<String> newWorkingSites;
        
        // Builder pattern
        public static MonitoringReportBuilder builder() {
            return new MonitoringReportBuilder();
        }
        
        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public int getTotalSites() { return totalSites; }
        public void setTotalSites(int totalSites) { this.totalSites = totalSites; }
        
        public int getWorkingSitesCount() { return workingSitesCount; }
        public void setWorkingSitesCount(int workingSitesCount) { this.workingSitesCount = workingSitesCount; }
        
        public int getDownSitesCount() { return downSitesCount; }
        public void setDownSitesCount(int downSitesCount) { this.downSitesCount = downSitesCount; }
        
        public double getUptimePercentage() { return uptimePercentage; }
        public void setUptimePercentage(double uptimePercentage) { this.uptimePercentage = uptimePercentage; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public Map<String, String> getSiteStatuses() { return siteStatuses; }
        public void setSiteStatuses(Map<String, String> siteStatuses) { this.siteStatuses = siteStatuses; }
        
        public List<String> getDownSites() { return downSites; }
        public void setDownSites(List<String> downSites) { this.downSites = downSites; }
        
        public List<String> getNewWorkingSites() { return newWorkingSites; }
        public void setNewWorkingSites(List<String> newWorkingSites) { this.newWorkingSites = newWorkingSites; }
        
        public static class MonitoringReportBuilder {
            private MonitoringReport report = new MonitoringReport();
            
            public MonitoringReportBuilder timestamp(LocalDateTime timestamp) {
                report.setTimestamp(timestamp);
                return this;
            }
            
            public MonitoringReportBuilder totalSites(int totalSites) {
                report.setTotalSites(totalSites);
                return this;
            }
            
            public MonitoringReportBuilder workingSitesCount(int count) {
                report.setWorkingSitesCount(count);
                return this;
            }
            
            public MonitoringReportBuilder downSitesCount(int count) {
                report.setDownSitesCount(count);
                return this;
            }
            
            public MonitoringReportBuilder uptimePercentage(double percentage) {
                report.setUptimePercentage(percentage);
                return this;
            }
            
            public MonitoringReportBuilder averageResponseTime(double time) {
                report.setAverageResponseTime(time);
                return this;
            }
            
            public MonitoringReportBuilder siteStatuses(Map<String, String> statuses) {
                report.setSiteStatuses(statuses);
                return this;
            }
            
            public MonitoringReportBuilder downSites(List<String> sites) {
                report.setDownSites(sites);
                return this;
            }
            
            public MonitoringReportBuilder newWorkingSites(List<String> sites) {
                report.setNewWorkingSites(sites);
                return this;
            }
            
            public MonitoringReport build() {
                return report;
            }
        }
    }
    
    public static class SystemHealthStatus {
        private String status;
        private int totalSites;
        private int workingSites;
        private int downSites;
        private double uptimePercentage;
        private double averageResponseTime;
        private LocalDateTime lastChecked;
        
        public static SystemHealthStatusBuilder builder() {
            return new SystemHealthStatusBuilder();
        }
        
        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public int getTotalSites() { return totalSites; }
        public void setTotalSites(int totalSites) { this.totalSites = totalSites; }
        
        public int getWorkingSites() { return workingSites; }
        public void setWorkingSites(int workingSites) { this.workingSites = workingSites; }
        
        public int getDownSites() { return downSites; }
        public void setDownSites(int downSites) { this.downSites = downSites; }
        
        public double getUptimePercentage() { return uptimePercentage; }
        public void setUptimePercentage(double uptimePercentage) { this.uptimePercentage = uptimePercentage; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public LocalDateTime getLastChecked() { return lastChecked; }
        public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }
        
        public static class SystemHealthStatusBuilder {
            private SystemHealthStatus status = new SystemHealthStatus();
            
            public SystemHealthStatusBuilder status(String statusValue) {
                status.setStatus(statusValue);
                return this;
            }
            
            public SystemHealthStatusBuilder totalSites(int total) {
                status.setTotalSites(total);
                return this;
            }
            
            public SystemHealthStatusBuilder workingSites(int working) {
                status.setWorkingSites(working);
                return this;
            }
            
            public SystemHealthStatusBuilder downSites(int down) {
                status.setDownSites(down);
                return this;
            }
            
            public SystemHealthStatusBuilder uptimePercentage(double percentage) {
                status.setUptimePercentage(percentage);
                return this;
            }
            
            public SystemHealthStatusBuilder averageResponseTime(double time) {
                status.setAverageResponseTime(time);
                return this;
            }
            
            public SystemHealthStatusBuilder lastChecked(LocalDateTime time) {
                status.setLastChecked(time);
                return this;
            }
            
            public SystemHealthStatus build() {
                return status;
            }
        }
    }
}