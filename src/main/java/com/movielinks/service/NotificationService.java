package com.movielinks.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${notifications.telegram.bot-token:}")
    private String telegramBotToken;
    
    @Value("${notifications.telegram.chat-id:}")
    private String telegramChatId;
    
    @Value("${notifications.slack.webhook-url:}")
    private String slackWebhookUrl;
    
    @Value("${notifications.discord.webhook-url:}")
    private String discordWebhookUrl;
    
    @Value("${notifications.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;
    
    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Send alert notification through multiple channels
     */
    public void sendAlert(String message, String severity) {
        logger.info("Sending {} alert: {}", severity, message);
        
        try {
            // Send to Telegram if configured
            if (!telegramBotToken.isEmpty() && !telegramChatId.isEmpty()) {
                sendTelegramNotification(message, severity);
            }
            
            // Send to Slack if configured
            if (!slackWebhookUrl.isEmpty()) {
                sendSlackNotification(message, severity);
            }
            
            // Send to Discord if configured
            if (!discordWebhookUrl.isEmpty()) {
                sendDiscordNotification(message, severity);
            }
            
            // Send to n8n for custom workflows
            sendN8nNotification(message, severity);
            
        } catch (Exception e) {
            logger.error("Error sending notifications: {}", e.getMessage());
        }
    }
    
    /**
     * Send Telegram notification
     */
    private void sendTelegramNotification(String message, String severity) {
        try {
            String emoji = getSeverityEmoji(severity);
            String formattedMessage = String.format("%s *Movie Sites Alert*\n\n%s\n\n_Time: %s_", 
                emoji, message, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            String url = String.format("https://api.telegram.org/bot%s/sendMessage", telegramBotToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> payload = Map.of(
                "chat_id", telegramChatId,
                "text", formattedMessage,
                "parse_mode", "Markdown"
            );
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("Telegram notification sent successfully");
            }
            
        } catch (Exception e) {
            logger.error("Failed to send Telegram notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send Slack notification
     */
    private void sendSlackNotification(String message, String severity) {
        try {
            String color = getSeverityColor(severity);
            
            Map<String, Object> attachment = Map.of(
                "color", color,
                "title", "Movie Sites Alert",
                "text", message,
                "footer", "Movie Site Monitor",
                "ts", System.currentTimeMillis() / 1000
            );
            
            Map<String, Object> payload = Map.of(
                "username", "Movie Site Monitor",
                "icon_emoji", ":warning:",
                "attachments", new Object[]{attachment}
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(slackWebhookUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("Slack notification sent successfully");
            }
            
        } catch (Exception e) {
            logger.error("Failed to send Slack notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send Discord notification
     */
    private void sendDiscordNotification(String message, String severity) {
        try {
            String emoji = getSeverityEmoji(severity);
            int color = getSeverityColorInt(severity);
            
            Map<String, Object> embed = Map.of(
                "title", emoji + " Movie Sites Alert",
                "description", message,
                "color", color,
                "timestamp", LocalDateTime.now().toString(),
                "footer", Map.of("text", "Movie Site Monitor")
            );
            
            Map<String, Object> payload = Map.of(
                "username", "Movie Site Monitor",
                "embeds", new Object[]{embed}
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(discordWebhookUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("Discord notification sent successfully");
            }
            
        } catch (Exception e) {
            logger.error("Failed to send Discord notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send notification to n8n for custom workflows
     */
    private void sendN8nNotification(String message, String severity) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> payload = Map.of(
                "action", "notification",
                "message", message,
                "severity", severity,
                "timestamp", LocalDateTime.now().toString(),
                "source", "movie-site-monitor"
            );
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            String webhookUrl = n8nWebhookUrl + "/webhook/notifications";
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("n8n notification sent successfully");
            }
            
        } catch (Exception e) {
            logger.warn("Failed to send n8n notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send daily summary report
     */
    public void sendDailySummary(Map<String, Object> summaryData) {
        try {
            StringBuilder summary = new StringBuilder();
            summary.append("📊 *Daily Movie Sites Summary*\n\n");
            
            if (summaryData.containsKey("totalSites")) {
                summary.append("🎬 Total Sites: ").append(summaryData.get("totalSites")).append("\n");
            }
            
            if (summaryData.containsKey("workingSites")) {
                summary.append("✅ Working: ").append(summaryData.get("workingSites")).append("\n");
            }
            
            if (summaryData.containsKey("downSites")) {
                summary.append("❌ Down: ").append(summaryData.get("downSites")).append("\n");
            }
            
            if (summaryData.containsKey("uptimePercentage")) {
                summary.append("📈 Uptime: ").append(String.format("%.1f%%", summaryData.get("uptimePercentage"))).append("\n");
            }
            
            if (summaryData.containsKey("averageResponseTime")) {
                summary.append("⚡ Avg Response: ").append(String.format("%.0fms", summaryData.get("averageResponseTime"))).append("\n");
            }
            
            summary.append("\n_Generated at: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("_");
            
            sendAlert(summary.toString(), "INFO");
            
        } catch (Exception e) {
            logger.error("Failed to send daily summary: {}", e.getMessage());
        }
    }
    
    /**
     * Send site recovery notification
     */
    public void sendSiteRecoveryNotification(String siteName, String newUrl) {
        String message = String.format("🎉 *Site Recovery Alert*\n\n" +
            "Site: %s\n" +
            "Status: Back Online ✅\n" +
            "New URL: %s\n\n" +
            "The site is now accessible again!", siteName, newUrl);
        
        sendAlert(message, "INFO");
    }
    
    /**
     * Send site down notification
     */
    public void sendSiteDownNotification(String siteName, String lastWorkingUrl) {
        String message = String.format("🚨 *Site Down Alert*\n\n" +
            "Site: %s\n" +
            "Status: Not Accessible ❌\n" +
            "Last Working URL: %s\n\n" +
            "The site appears to be down or blocked.", siteName, lastWorkingUrl);
        
        sendAlert(message, "CRITICAL");
    }
    
    /**
     * Send new domain found notification
     */
    public void sendNewDomainNotification(String siteName, String oldUrl, String newUrl) {
        String message = String.format("🔄 *New Domain Found*\n\n" +
            "Site: %s\n" +
            "Old URL: %s\n" +
            "New URL: %s\n\n" +
            "The site has moved to a new domain!", siteName, oldUrl, newUrl);
        
        sendAlert(message, "INFO");
    }
    
    /**
     * Test notification system
     */
    public void sendTestNotification() {
        String message = "🧪 *Test Notification*\n\n" +
            "This is a test message from the Movie Site Monitor.\n" +
            "If you receive this, notifications are working correctly!";
        
        sendAlert(message, "INFO");
    }
    
    /**
     * Get emoji for severity level
     */
    private String getSeverityEmoji(String severity) {
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                return "🚨";
            case "WARNING":
                return "⚠️";
            case "INFO":
                return "ℹ️";
            case "SUCCESS":
                return "✅";
            default:
                return "📢";
        }
    }
    
    /**
     * Get color for severity level (Slack)
     */
    private String getSeverityColor(String severity) {
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                return "danger";
            case "WARNING":
                return "warning";
            case "INFO":
                return "good";
            case "SUCCESS":
                return "good";
            default:
                return "#36a64f";
        }
    }
    
    /**
     * Get color integer for severity level (Discord)
     */
    private int getSeverityColorInt(String severity) {
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                return 15158332; // Red
            case "WARNING":
                return 15105570; // Orange
            case "INFO":
                return 3447003;  // Blue
            case "SUCCESS":
                return 3066993;  // Green
            default:
                return 9807270;  // Gray
        }
    }
}