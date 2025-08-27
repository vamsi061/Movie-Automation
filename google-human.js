// Human-like Puppeteer script for Browserless
const puppeteer = require("puppeteer-extra");
const StealthPlugin = require("puppeteer-extra-plugin-stealth");

// Enable stealth mode
puppeteer.use(StealthPlugin());

// Helper: simulate human typing
async function humanType(page, selector, text) {
  await page.focus(selector);
  for (const char of text) {
    await page.keyboard.type(char);
    await page.waitForTimeout(100 + Math.random() * 200); // random delay
  }
}

// Helper: simulate human mouse movement
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

// Main function
module.exports = async ({ page }) => {
  // Go to Google
  await page.goto("https://www.google.com", { waitUntil: "domcontentloaded" });

  // Move mouse randomly before typing (looks human)
  await humanMouseMove(page, 200, 200);

  // Type slowly into the search box
  await humanType(page, "input[name='q']", "best free ai tools");

  // Small pause (like thinking)
  await page.waitForTimeout(1500);

  // Press Enter
  await page.keyboard.press("Enter");

  // Scroll down slowly like a human reading
  for (let i = 0; i < 5; i++) {
    await page.evaluate(() => window.scrollBy(0, 200));
    await page.waitForTimeout(800 + Math.random() * 1000);
  }

  // Wait for results
  await page.waitForSelector("h3");

  // Extract first 5 results
  const results = await page.evaluate(() => {
    return Array.from(document.querySelectorAll("h3"))
      .slice(0, 5)
      .map(el => el.innerText);
  });

  return results;
};