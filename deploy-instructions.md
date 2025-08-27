# 🚀 Production Deployment Guide

## 📋 Overview
This guide will help you deploy the Movie Site Search system to production:
- **Frontend**: Vercel (Next.js)
- **Backend**: Fly.io (Spring Boot)
- **Automation**: n8n (Already deployed at https://n8n-7j94.onrender.com)

## 🔧 Backend Deployment (Fly.io)

### 1. Install Fly CLI
```bash
# Install Fly CLI
curl -L https://fly.io/install.sh | sh

# Login to Fly.io
flyctl auth login
```

### 2. Initialize Fly App
```bash
# Create new app (run from project root)
flyctl apps create movie-site-search-api

# Set secrets
flyctl secrets set BROWSERLESS_API_KEY=your_browserless_api_key_here
flyctl secrets set BROWSERLESS_URL=https://chrome.browserless.io
flyctl secrets set N8N_WEBHOOK_URL=https://n8n-7j94.onrender.com
```

### 3. Deploy to Fly.io
```bash
# Deploy the application
flyctl deploy

# Check deployment status
flyctl status

# View logs
flyctl logs
```

### 4. Get your Fly.io URL
```bash
# Your API will be available at:
# https://movie-site-search-api.fly.dev
```

## 🌐 Frontend Deployment (Vercel)

### 1. Prepare Frontend
```bash
cd frontend

# Install dependencies
npm install

# Test build locally
npm run build
```

### 2. Deploy to Vercel

#### Option A: Vercel CLI
```bash
# Install Vercel CLI
npm i -g vercel

# Login to Vercel
vercel login

# Deploy
vercel --prod
```

#### Option B: GitHub Integration
1. Push code to GitHub repository
2. Go to [vercel.com](https://vercel.com)
3. Import your GitHub repository
4. Set environment variables in Vercel dashboard

### 3. Set Environment Variables in Vercel
In your Vercel dashboard, add these environment variables:

```
NEXT_PUBLIC_API_URL=https://movie-site-search-api.fly.dev
NEXT_PUBLIC_N8N_URL=https://n8n-7j94.onrender.com
```

## 🔄 n8n Workflow Setup

### 1. Access your n8n instance
Visit: https://n8n-7j94.onrender.com

### 2. Create Movie Site Search Workflow

#### Example Workflow 1: Daily Site Check
```json
{
  "name": "Daily Movie Site Check",
  "nodes": [
    {
      "name": "Schedule",
      "type": "n8n-nodes-base.cron",
      "parameters": {
        "triggerTimes": {
          "hour": 9,
          "minute": 0
        }
      }
    },
    {
      "name": "Search Movie Sites",
      "type": "n8n-nodes-base.httpRequest",
      "parameters": {
        "url": "https://movie-site-search-api.fly.dev/api/movie-sites/search/all",
        "method": "GET"
      }
    },
    {
      "name": "Process Results",
      "type": "n8n-nodes-base.function",
      "parameters": {
        "functionCode": "const results = items[0].json.results;\nconst workingSites = results.filter(site => site.status === 'WORKING');\nreturn [{ json: { workingSites, totalWorking: workingSites.length } }];"
      }
    }
  ]
}
```

#### Example Workflow 2: Webhook Integration
```json
{
  "name": "Movie Site Search Webhook",
  "nodes": [
    {
      "name": "Webhook",
      "type": "n8n-nodes-base.webhook",
      "parameters": {
        "path": "movie-search",
        "httpMethod": "POST"
      }
    },
    {
      "name": "Search Sites",
      "type": "n8n-nodes-base.httpRequest",
      "parameters": {
        "url": "https://movie-site-search-api.fly.dev/api/movie-sites/webhook/n8n",
        "method": "POST",
        "body": {
          "action": "search_multiple",
          "siteNames": "={{ $json.siteNames }}"
        }
      }
    }
  ]
}
```

## 🔗 API Endpoints (Production)

### Base URL
```
https://movie-site-search-api.fly.dev
```

### Key Endpoints
```bash
# Health check
GET /api/movie-sites/health

# Search single site
GET /api/movie-sites/search/movierulz

# Search multiple sites
POST /api/movie-sites/search/batch
{
  "siteNames": ["movierulz", "moviezap"]
}

# Search all popular sites
GET /api/movie-sites/search/all

# n8n webhook
POST /api/movie-sites/webhook/n8n
{
  "action": "search_site",
  "siteName": "movierulz"
}
```

## 🧪 Testing Production Deployment

### 1. Test Backend API
```bash
# Test health endpoint
curl https://movie-site-search-api.fly.dev/api/movie-sites/health

# Test single site search
curl https://movie-site-search-api.fly.dev/api/movie-sites/search/movierulz

# Test batch search
curl -X POST https://movie-site-search-api.fly.dev/api/movie-sites/search/batch \
  -H "Content-Type: application/json" \
  -d '{"siteNames": ["movierulz", "moviezap"]}'
```

### 2. Test Frontend
Visit your Vercel URL and test:
- Single site search
- Batch search functionality
- Results display

### 3. Test n8n Integration
```bash
# Test n8n webhook
curl -X POST https://movie-site-search-api.fly.dev/api/movie-sites/webhook/n8n \
  -H "Content-Type: application/json" \
  -d '{"action": "search_site", "siteName": "movierulz"}'
```

## 📊 Monitoring & Maintenance

### Fly.io Monitoring
```bash
# View app status
flyctl status

# View logs
flyctl logs

# Scale app if needed
flyctl scale count 2

# View metrics
flyctl dashboard
```

### Vercel Monitoring
- Check deployment logs in Vercel dashboard
- Monitor function execution times
- Check analytics for usage patterns

### n8n Monitoring
- Monitor workflow executions
- Check for failed runs
- Set up notifications for critical workflows

## 🔧 Troubleshooting

### Common Issues

#### Backend Issues
```bash
# Check app status
flyctl status

# View recent logs
flyctl logs --app movie-site-search-api

# Restart app
flyctl apps restart movie-site-search-api
```

#### Frontend Issues
- Check Vercel deployment logs
- Verify environment variables are set
- Test API connectivity from browser console

#### CORS Issues
If you encounter CORS errors, update the backend CORS configuration in `application-production.yml`:

```yaml
cors:
  allowed-origins: 
    - https://your-actual-vercel-domain.vercel.app
```

## 🚀 Going Live Checklist

- [ ] Backend deployed to Fly.io
- [ ] Frontend deployed to Vercel
- [ ] Environment variables configured
- [ ] API endpoints tested
- [ ] Frontend functionality tested
- [ ] n8n workflows created
- [ ] Monitoring set up
- [ ] CORS configured properly
- [ ] Health checks working

## 📞 Support

If you encounter issues:
1. Check the logs first
2. Verify environment variables
3. Test API endpoints individually
4. Check n8n workflow execution logs

Your production system will be:
- **Frontend**: https://your-app.vercel.app
- **Backend**: https://movie-site-search-api.fly.dev
- **n8n**: https://n8n-7j94.onrender.com