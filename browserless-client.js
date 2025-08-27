// Browserless API Client
const axios = require('axios');
require('dotenv').config();

class BrowserlessClient {
  constructor() {
    this.apiKey = process.env.BROWSERLESS_API_KEY;
    this.baseUrl = process.env.BROWSERLESS_URL || 'https://chrome.browserless.io';
    
    if (!this.apiKey) {
      throw new Error('BROWSERLESS_API_KEY is required');
    }
  }

  /**
   * Execute a Puppeteer script on Browserless
   * @param {string} script - The Puppeteer script to execute
   * @param {object} options - Additional options
   * @returns {Promise<any>} - Script execution result
   */
  async executeScript(script, options = {}) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/function?token=${this.apiKey}`,
        {
          code: script,
          context: options.context || {},
          ...options
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
          },
          timeout: options.timeout || 60000
        }
      );

      return response.data;
    } catch (error) {
      console.error('Browserless execution error:', error.response?.data || error.message);
      throw new Error(`Browserless execution failed: ${error.message}`);
    }
  }

  /**
   * Execute the Google search script
   * @param {string} query - Search query
   * @param {object} options - Additional options
   * @returns {Promise<string[]>} - Array of search results
   */
  async googleSearch(query, options = {}) {
    const googleScript = `
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
      
      async function humanMouseMove(page, x, y) {
        const steps = 20 + Math.floor(Math.random() * 10);
        const start = await page.mouse._x || 0;
        const end = await page.mouse._y || 0;
      
        for (let i = 0; i < steps; i++) {
          const nx = start + ((x - start) * i) / steps + Math.random() * 2;
          const ny = end + ((y - end) * i) / steps + Math.random() * 2;
          await page.mouse.move(nx, ny);
          await page.waitForTimeout(10 + Math.random() * 30);
        }
      }
      
      module.exports = async ({ page }) => {
        await page.goto("https://www.google.com", { waitUntil: "domcontentloaded" });
        await humanMouseMove(page, 200, 200);
        await humanType(page, "input[name='q']", "${query}");
        await page.waitForTimeout(1500);
        await page.keyboard.press("Enter");
        
        for (let i = 0; i < 5; i++) {
          await page.evaluate(() => window.scrollBy(0, 200));
          await page.waitForTimeout(800 + Math.random() * 1000);
        }
        
        await page.waitForSelector("h3");
        
        const results = await page.evaluate(() => {
          return Array.from(document.querySelectorAll("h3"))
            .slice(0, ${options.maxResults || 5})
            .map(el => ({
              title: el.innerText,
              link: el.closest('a')?.href || null
            }));
        });
        
        return results;
      };
    `;

    return await this.executeScript(googleScript, options);
  }

  /**
   * Scrape a specific URL
   * @param {string} url - URL to scrape
   * @param {object} selectors - CSS selectors to extract data
   * @param {object} options - Additional options
   * @returns {Promise<object>} - Scraped data
   */
  async scrapeUrl(url, selectors = {}, options = {}) {
    const scrapeScript = `
      module.exports = async ({ page }) => {
        await page.goto("${url}", { 
          waitUntil: "domcontentloaded",
          timeout: 30000 
        });
        
        // Wait for content to load
        await page.waitForTimeout(2000);
        
        const data = {};
        const selectors = ${JSON.stringify(selectors)};
        
        for (const [key, selector] of Object.entries(selectors)) {
          try {
            if (selector.multiple) {
              data[key] = await page.evaluate((sel) => {
                return Array.from(document.querySelectorAll(sel.selector))
                  .map(el => el.innerText || el.textContent);
              }, selector);
            } else {
              data[key] = await page.evaluate((sel) => {
                const element = document.querySelector(sel.selector || sel);
                return element ? (element.innerText || element.textContent) : null;
              }, selector);
            }
          } catch (error) {
            data[key] = null;
          }
        }
        
        return data;
      };
    `;

    return await this.executeScript(scrapeScript, options);
  }

  /**
   * Take a screenshot of a page
   * @param {string} url - URL to screenshot
   * @param {object} options - Screenshot options
   * @returns {Promise<Buffer>} - Screenshot buffer
   */
  async screenshot(url, options = {}) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/screenshot?token=${this.apiKey}`,
        {
          url: url,
          options: {
            fullPage: options.fullPage || false,
            type: options.type || 'png',
            quality: options.quality || 80,
            ...options
          }
        },
        {
          responseType: 'arraybuffer',
          timeout: options.timeout || 30000
        }
      );

      return Buffer.from(response.data);
    } catch (error) {
      console.error('Screenshot error:', error.response?.data || error.message);
      throw new Error(`Screenshot failed: ${error.message}`);
    }
  }
}

module.exports = BrowserlessClient;