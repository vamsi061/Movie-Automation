package com.movielinks.controller;

import com.movielinks.model.MovieSite;
import com.movielinks.service.MovieSiteSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/movie-sites")
@CrossOrigin(origins = "*")
public class MovieSiteController {
    
    private static final Logger logger = LoggerFactory.getLogger(MovieSiteController.class);
    
    @Autowired
    private MovieSiteSearchService movieSiteSearchService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "movie-site-search-api");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search for a single movie site working link
     */
    @GetMapping("/search/{siteName}")
    public ResponseEntity<Map<String, Object>> searchSite(@PathVariable String siteName) {
        logger.info("Received request to search for site: {}", siteName);
        
        try {
            MovieSite result = movieSiteSearchService.findWorkingLink(siteName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("siteName", siteName);
            response.put("result", result);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching for site {}: {}", siteName, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("siteName", siteName);
            response.put("error", "Search failed: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Search for multiple movie sites
     */
    @PostMapping("/search/batch")
    public ResponseEntity<Map<String, Object>> searchMultipleSites(@RequestBody Map<String, List<String>> request) {
        List<String> siteNames = request.get("siteNames");
        
        if (siteNames == null || siteNames.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "siteNames array is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        logger.info("Received batch request for {} sites: {}", siteNames.size(), siteNames);
        
        try {
            List<MovieSite> results = movieSiteSearchService.findWorkingLinks(siteNames);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalSites", siteNames.size());
            response.put("results", results);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in batch search: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Batch search failed: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Search for Movierulz specifically
     */
    @GetMapping("/movierulz")
    public ResponseEntity<Map<String, Object>> searchMovierulz() {
        return searchSite("movierulz");
    }
    
    /**
     * Search for Moviezap specifically
     */
    @GetMapping("/moviezap")
    public ResponseEntity<Map<String, Object>> searchMoviezap() {
        return searchSite("moviezap");
    }
    
    /**
     * Search for all popular movie sites
     */
    @GetMapping("/search/all")
    public ResponseEntity<Map<String, Object>> searchAllPopularSites() {
        List<String> popularSites = List.of(
            "movierulz",
            "moviezap",
            "tamilrockers",
            "filmywap",
            "worldfree4u",
            "9xmovies",
            "khatrimaza",
            "bolly4u"
        );
        
        Map<String, List<String>> request = Map.of("siteNames", popularSites);
        return searchMultipleSites(request);
    }
    
    /**
     * Get supported movie sites list
     */
    @GetMapping("/supported")
    public ResponseEntity<Map<String, Object>> getSupportedSites() {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> supportedSites = List.of(
            Map.of(
                "name", "movierulz",
                "description", "Popular movie streaming site",
                "searchAliases", List.of("movierulz", "movie rulz", "movierulz.com")
            ),
            Map.of(
                "name", "moviezap",
                "description", "Movie download and streaming platform",
                "searchAliases", List.of("moviezap", "movie zap", "moviezap.com")
            ),
            Map.of(
                "name", "tamilrockers",
                "description", "Tamil and other regional movies",
                "searchAliases", List.of("tamilrockers", "tamil rockers")
            ),
            Map.of(
                "name", "filmywap",
                "description", "Bollywood and Hollywood movies",
                "searchAliases", List.of("filmywap", "filmy wap")
            )
        );
        
        response.put("success", true);
        response.put("supportedSites", supportedSites);
        response.put("totalCount", supportedSites.size());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Webhook endpoint for n8n integration
     */
    @PostMapping("/webhook/n8n")
    public ResponseEntity<Map<String, Object>> n8nWebhook(@RequestBody Map<String, Object> request) {
        logger.info("Received n8n webhook request: {}", request);
        
        try {
            String action = (String) request.get("action");
            
            if ("search_site".equals(action)) {
                String siteName = (String) request.get("siteName");
                if (siteName == null) {
                    throw new IllegalArgumentException("siteName is required for search_site action");
                }
                
                MovieSite result = movieSiteSearchService.findWorkingLink(siteName);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("action", action);
                response.put("result", result);
                response.put("timestamp", LocalDateTime.now());
                
                return ResponseEntity.ok(response);
                
            } else if ("search_multiple".equals(action)) {
                @SuppressWarnings("unchecked")
                List<String> siteNames = (List<String>) request.get("siteNames");
                if (siteNames == null || siteNames.isEmpty()) {
                    throw new IllegalArgumentException("siteNames array is required for search_multiple action");
                }
                
                List<MovieSite> results = movieSiteSearchService.findWorkingLinks(siteNames);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("action", action);
                response.put("results", results);
                response.put("timestamp", LocalDateTime.now());
                
                return ResponseEntity.ok(response);
                
            } else {
                throw new IllegalArgumentException("Invalid action: " + action);
            }
            
        } catch (Exception e) {
            logger.error("Error processing n8n webhook: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Webhook processing failed: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}