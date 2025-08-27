package com.movielinks.service;

import com.movielinks.model.MovieSite;
import com.movielinks.model.Movierulz;
import com.movielinks.model.Moviezap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MovieSiteSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(MovieSiteSearchService.class);
    
    @Value("${browserless.api.key}")
    private String browserlessApiKey;
    
    @Value("${browserless.api.url:https://chrome.browserless.io}")
    private String browserlessUrl;
    
    private final RestTemplate restTemplate;
    
    // URL validation patterns
    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?",
        Pattern.CASE_INSENSITIVE
    );
    
    // Domain patterns for movie sites
    private static final Map<String, Pattern> DOMAIN_PATTERNS = Map.of(
        "movierulz", Pattern.compile("movierulz\\.[a-z]{2,4}", Pattern.CASE_INSENSITIVE),
        "moviezap", Pattern.compile("moviezap\\.[a-z]{2,4}", Pattern.CASE_INSENSITIVE)
    );
    
    public MovieSiteSearchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Search for working links of a specific movie site
     */
    public MovieSite findWorkingLink(String siteName) {
        logger.info("Searching for working link for: {}", siteName);
        
        try {
            // Create appropriate model instance
            MovieSite movieSite = createMovieSiteInstance(siteName);
            
            // Search on Google first
            List<String> googleResults = searchOnGoogle(movieSite.getSearchAliases());
            
            // Search on DuckDuckGo as backup
            List<String> duckDuckGoResults = searchOnDuckDuckGo(movieSite.getSearchAliases());
            
            // Combine and validate results
            Set<String> allUrls = new HashSet<>();
            allUrls.addAll(googleResults);
            allUrls.addAll(duckDuckGoResults);
            
            // Find the best working URL
            String workingUrl = validateAndFindBestUrl(allUrls, siteName);
            
            if (workingUrl != null) {
                movieSite.setCurrentWorkingUrl(workingUrl);
                movieSite.setStatus("WORKING");
                movieSite.setLastChecked(LocalDateTime.now());
                logger.info("Found working URL for {}: {}", siteName, workingUrl);
            } else {
                movieSite.setStatus("NOT_FOUND");
                logger.warn("No working URL found for: {}", siteName);
            }
            
            return movieSite;
            
        } catch (Exception e) {
            logger.error("Error searching for {}: {}", siteName, e.getMessage());
            MovieSite errorSite = createMovieSiteInstance(siteName);
            errorSite.setStatus("ERROR");
            errorSite.setNotes("Search failed: " + e.getMessage());
            return errorSite;
        }
    }
    
    /**
     * Search for multiple movie sites
     */
    public List<MovieSite> findWorkingLinks(List<String> siteNames) {
        List<MovieSite> results = new ArrayList<>();
        
        for (String siteName : siteNames) {
            try {
                MovieSite result = findWorkingLink(siteName);
                results.add(result);
                
                // Add delay between searches to avoid rate limiting
                Thread.sleep(2000);
                
            } catch (Exception e) {
                logger.error("Error processing site {}: {}", siteName, e.getMessage());
                MovieSite errorSite = createMovieSiteInstance(siteName);
                errorSite.setStatus("ERROR");
                results.add(errorSite);
            }
        }
        
        return results;
    }
    
    /**
     * Search on Google using Browserless
     */
    private List<String> searchOnGoogle(List<String> searchTerms) {
        List<String> urls = new ArrayList<>();
        
        for (String term : searchTerms) {
            try {
                String searchScript = createGoogleSearchScript(term);
                String response = executeBrowserlessScript(searchScript);
                
                List<String> extractedUrls = extractUrlsFromResponse(response);
                urls.addAll(extractedUrls);
                
                // Add delay between searches
                Thread.sleep(1500);
                
            } catch (Exception e) {
                logger.warn("Google search failed for term '{}': {}", term, e.getMessage());
            }
        }
        
        return urls;
    }
    
    /**
     * Search on DuckDuckGo using Browserless
     */
    private List<String> searchOnDuckDuckGo(List<String> searchTerms) {
        List<String> urls = new ArrayList<>();
        
        for (String term : searchTerms) {
            try {
                String searchScript = createDuckDuckGoSearchScript(term);
                String response = executeBrowserlessScript(searchScript);
                
                List<String> extractedUrls = extractUrlsFromResponse(response);
                urls.addAll(extractedUrls);
                
                // Add delay between searches
                Thread.sleep(1500);
                
            } catch (Exception e) {
                logger.warn("DuckDuckGo search failed for term '{}': {}", term, e.getMessage());
            }
        }
        
        return urls;
    }
    
    /**
     * Create Google search script for Browserless
     */
    private String createGoogleSearchScript(String searchTerm) {
        return String.format("""
            const puppeteer = require("puppeteer-extra");
            const StealthPlugin = require("puppeteer-extra-plugin-stealth");
            
            puppeteer.use(StealthPlugin());
            
            async function humanType(page, selector, text) {
                await page.focus(selector);
                for (const char of text) {
                    await page.keyboard.type(char);
                    await page.waitForTimeout(100 + Math.random() * 200);
                }
            }
            
            module.exports = async ({ page }) => {
                await page.goto("https://www.google.com", { waitUntil: "domcontentloaded" });
                
                // Accept cookies if present
                try {
                    await page.click('button[id="L2AGLb"]', { timeout: 3000 });
                } catch (e) {}
                
                await humanType(page, "input[name='q']", "%s");
                await page.waitForTimeout(1500);
                await page.keyboard.press("Enter");
                
                await page.waitForSelector("h3", { timeout: 10000 });
                
                const results = await page.evaluate(() => {
                    return Array.from(document.querySelectorAll("h3"))
                        .slice(0, 10)
                        .map(el => {
                            const link = el.closest('a');
                            return {
                                title: el.innerText,
                                url: link ? link.href : null
                            };
                        })
                        .filter(result => result.url);
                });
                
                return JSON.stringify(results);
            };
            """, searchTerm);
    }
    
    /**
     * Create DuckDuckGo search script for Browserless
     */
    private String createDuckDuckGoSearchScript(String searchTerm) {
        return String.format("""
            const puppeteer = require("puppeteer-extra");
            const StealthPlugin = require("puppeteer-extra-plugin-stealth");
            
            puppeteer.use(StealthPlugin());
            
            module.exports = async ({ page }) => {
                await page.goto("https://duckduckgo.com", { waitUntil: "domcontentloaded" });
                
                await page.type("input[name='q']", "%s");
                await page.waitForTimeout(1000);
                await page.keyboard.press("Enter");
                
                await page.waitForSelector("h2 a", { timeout: 10000 });
                
                const results = await page.evaluate(() => {
                    return Array.from(document.querySelectorAll("h2 a"))
                        .slice(0, 10)
                        .map(el => ({
                            title: el.innerText,
                            url: el.href
                        }))
                        .filter(result => result.url);
                });
                
                return JSON.stringify(results);
            };
            """, searchTerm);
    }
    
    /**
     * Execute script on Browserless
     */
    private String executeBrowserlessScript(String script) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = Map.of(
            "code", script,
            "context", Map.of()
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        String url = browserlessUrl + "/function?token=" + browserlessApiKey;
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return response.getBody();
    }
    
    /**
     * Extract URLs from Browserless response
     */
    private List<String> extractUrlsFromResponse(String response) {
        List<String> urls = new ArrayList<>();
        
        try {
            // Parse JSON response and extract URLs
            Matcher matcher = URL_PATTERN.matcher(response);
            while (matcher.find()) {
                String url = matcher.group();
                if (isValidMovieSiteUrl(url)) {
                    urls.add(url);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract URLs from response: {}", e.getMessage());
        }
        
        return urls;
    }
    
    /**
     * Validate URLs and find the best working one
     */
    private String validateAndFindBestUrl(Set<String> urls, String siteName) {
        for (String url : urls) {
            if (isValidMovieSiteUrl(url) && matchesSiteName(url, siteName)) {
                if (isUrlAccessible(url)) {
                    return url;
                }
            }
        }
        return null;
    }
    
    /**
     * Check if URL is a valid movie site URL
     */
    private boolean isValidMovieSiteUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        
        // Check if it's a valid URL format
        if (!URL_PATTERN.matcher(url).matches()) return false;
        
        // Exclude common non-movie sites
        String lowerUrl = url.toLowerCase();
        return !lowerUrl.contains("google.com") &&
               !lowerUrl.contains("youtube.com") &&
               !lowerUrl.contains("facebook.com") &&
               !lowerUrl.contains("twitter.com") &&
               !lowerUrl.contains("instagram.com");
    }
    
    /**
     * Check if URL matches the site name
     */
    private boolean matchesSiteName(String url, String siteName) {
        Pattern pattern = DOMAIN_PATTERNS.get(siteName.toLowerCase());
        return pattern != null && pattern.matcher(url).find();
    }
    
    /**
     * Check if URL is accessible
     */
    private boolean isUrlAccessible(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.HEAD, entity, String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            logger.debug("URL not accessible: {} - {}", url, e.getMessage());
            return false;
        }
    }
    
    /**
     * Create appropriate MovieSite instance based on site name
     */
    private MovieSite createMovieSiteInstance(String siteName) {
        switch (siteName.toLowerCase()) {
            case "movierulz":
                return new Movierulz();
            case "moviezap":
                return new Moviezap();
            default:
                MovieSite site = new MovieSite();
                site.setSiteName(siteName);
                site.setSearchAliases(List.of(siteName, siteName + ".com", siteName + " latest"));
                return site;
        }
    }
}