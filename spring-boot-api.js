// Express.js API Server (Spring Boot equivalent in Node.js)
const express = require('express');
const cors = require('cors');
const BrowserlessClient = require('./browserless-client');
require('dotenv').config();

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Initialize Browserless client
const browserless = new BrowserlessClient();

// Middleware for API key authentication
const authenticateApiKey = (req, res, next) => {
  const apiKey = req.headers['x-api-key'] || req.query.apiKey;
  const validApiKey = process.env.SPRING_BOOT_API_KEY;
  
  if (validApiKey && apiKey !== validApiKey) {
    return res.status(401).json({ error: 'Invalid API key' });
  }
  
  next();
};

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ 
    status: 'healthy', 
    timestamp: new Date().toISOString(),
    service: 'browserless-scraping-api'
  });
});

// Google Search endpoint
app.post('/api/search/google', authenticateApiKey, async (req, res) => {
  try {
    const { query, maxResults = 5, filters = {} } = req.body;
    
    if (!query) {
      return res.status(400).json({ error: 'Query parameter is required' });
    }

    console.log(`Performing Google search for: "${query}"`);
    
    const results = await browserless.googleSearch(query, { 
      maxResults,
      timeout: 60000 
    });

    // Process and clean results
    const cleanedResults = results
      .filter(result => result.title && result.title.trim())
      .map(result => ({
        title: result.title.trim(),
        link: result.link,
        domain: result.link ? new URL(result.link).hostname : null
      }));

    // Apply filters if provided
    let filteredResults = cleanedResults;
    if (filters.excludeDomains) {
      filteredResults = filteredResults.filter(
        result => !filters.excludeDomains.includes(result.domain)
      );
    }

    res.json({
      success: true,
      query,
      resultsCount: filteredResults.length,
      results: filteredResults,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    console.error('Google search error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Search failed',
      message: error.message 
    });
  }
});

// Generic URL scraping endpoint
app.post('/api/scrape/url', authenticateApiKey, async (req, res) => {
  try {
    const { url, selectors = {}, options = {} } = req.body;
    
    if (!url) {
      return res.status(400).json({ error: 'URL parameter is required' });
    }

    console.log(`Scraping URL: ${url}`);
    
    const data = await browserless.scrapeUrl(url, selectors, {
      timeout: options.timeout || 30000
    });

    res.json({
      success: true,
      url,
      data,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    console.error('URL scraping error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Scraping failed',
      message: error.message 
    });
  }
});

// Screenshot endpoint
app.post('/api/screenshot', authenticateApiKey, async (req, res) => {
  try {
    const { url, options = {} } = req.body;
    
    if (!url) {
      return res.status(400).json({ error: 'URL parameter is required' });
    }

    console.log(`Taking screenshot of: ${url}`);
    
    const screenshot = await browserless.screenshot(url, options);

    res.set({
      'Content-Type': 'image/png',
      'Content-Length': screenshot.length,
      'Content-Disposition': `attachment; filename="screenshot-${Date.now()}.png"`
    });

    res.send(screenshot);

  } catch (error) {
    console.error('Screenshot error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Screenshot failed',
      message: error.message 
    });
  }
});

// Batch processing endpoint
app.post('/api/batch/search', authenticateApiKey, async (req, res) => {
  try {
    const { queries, maxResults = 5 } = req.body;
    
    if (!Array.isArray(queries) || queries.length === 0) {
      return res.status(400).json({ error: 'Queries array is required' });
    }

    console.log(`Processing batch search for ${queries.length} queries`);
    
    const results = [];
    
    // Process queries sequentially to avoid rate limiting
    for (const query of queries) {
      try {
        const searchResults = await browserless.googleSearch(query, { maxResults });
        results.push({
          query,
          success: true,
          results: searchResults
        });
        
        // Add delay between requests
        await new Promise(resolve => setTimeout(resolve, 2000));
        
      } catch (error) {
        results.push({
          query,
          success: false,
          error: error.message
        });
      }
    }

    res.json({
      success: true,
      totalQueries: queries.length,
      results,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    console.error('Batch search error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Batch search failed',
      message: error.message 
    });
  }
});

// n8n webhook integration endpoint
app.post('/api/webhook/n8n', async (req, res) => {
  try {
    const { action, data } = req.body;
    
    let result;
    
    switch (action) {
      case 'google_search':
        result = await browserless.googleSearch(data.query, data.options);
        break;
        
      case 'scrape_url':
        result = await browserless.scrapeUrl(data.url, data.selectors, data.options);
        break;
        
      case 'screenshot':
        result = await browserless.screenshot(data.url, data.options);
        // Convert buffer to base64 for JSON response
        result = result.toString('base64');
        break;
        
      default:
        return res.status(400).json({ error: 'Invalid action' });
    }

    res.json({
      success: true,
      action,
      result,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    console.error('n8n webhook error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Webhook processing failed',
      message: error.message 
    });
  }
});

// Error handling middleware
app.use((error, req, res, next) => {
  console.error('Unhandled error:', error);
  res.status(500).json({ 
    success: false,
    error: 'Internal server error',
    message: error.message 
  });
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({ 
    success: false,
    error: 'Endpoint not found' 
  });
});

// Start server
app.listen(port, () => {
  console.log(`🚀 Browserless Scraping API running on port ${port}`);
  console.log(`📊 Health check: http://localhost:${port}/health`);
  console.log(`🔍 Google Search: POST http://localhost:${port}/api/search/google`);
  console.log(`🌐 URL Scraping: POST http://localhost:${port}/api/scrape/url`);
  console.log(`📸 Screenshots: POST http://localhost:${port}/api/screenshot`);
});

module.exports = app;