# 🚀 Render Deployment Guide

Complete guide for deploying the Movie Site Search System backend to Render.

## 🎯 Why Render?

- **Simpler than Fly.io** - No CLI required
- **Free tier available** - Perfect for testing
- **Auto-deploy from GitHub** - Push to deploy
- **Built-in monitoring** - Health checks included
- **Easy environment variables** - Web dashboard

## 📋 Prerequisites

- GitHub repository with your code
- Render account (free at render.com)
- Browserless.io API key

## 🚀 Backend Deployment Steps

### Step 1: Create Render Account

1. Go to [render.com](https://render.com)
2. Sign up with GitHub
3. Connect your GitHub account

### Step 2: Create New Web Service

1. Click **"New +"** → **"Web Service"**
2. Connect your GitHub repository
3. Select your repository: `movie-site-search-system`

### Step 3: Configure Service Settings

**Basic Settings:**
```
Name: movie-site-search-api
Environment: Java
Region: Oregon (or closest to you)
Branch: main
```

**Build Settings:**
```
Build Command: mvn clean package -DskipTests
Start Command: java -jar target/movie-site-search-api-1.0.0.jar
```

**Advanced Settings:**
```
Health Check Path: /api/movie-sites/health
Auto-Deploy: Yes
```

### Step 4: Set Environment Variables

In the Render dashboard, add these environment variables:

**Required:**
```
PORT=10000
SPRING_PROFILES_ACTIVE=render
BROWSERLESS_API_KEY=your_browserless_api_key_here
BROWSERLESS_URL=https://chrome.browserless.io
N8N_WEBHOOK_URL=https://n8n-7j94.onrender.com
```

**Optional (for notifications):**
```
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_CHAT_ID=your_telegram_chat_id
SLACK_WEBHOOK_URL=your_slack_webhook_url
DISCORD_WEBHOOK_URL=your_discord_webhook_url
```

### Step 5: Deploy

1. Click **"Create Web Service"**
2. Render will automatically:
   - Clone your repository
   - Build the application
   - Deploy to a public URL
3. Wait for deployment to complete (5-10 minutes)

### Step 6: Get Your Backend URL

Your backend will be available at:
```
https://movie-site-search-api.onrender.com
```

## 🔧 Update Frontend Configuration

### Update Vercel Environment Variables

1. Go to Vercel dashboard
2. Update environment variable:
```
NEXT_PUBLIC_API_URL=https://movie-site-search-api.onrender.com
```
3. Redeploy frontend

### Update CORS Configuration

The backend is already configured to allow your Vercel domain.

## 🧪 Testing Deployment

### Test Health Endpoint
```bash
curl https://movie-site-search-api.onrender.com/api/movie-sites/health
```

### Test Search Endpoint
```bash
curl https://movie-site-search-api.onrender.com/api/movie-sites/search/movierulz
```

### Test Admin Dashboard
```bash
curl https://movie-site-search-api.onrender.com/api/admin/dashboard
```

## 📊 Render Dashboard Features

### Monitoring
- **Logs**: Real-time application logs
- **Metrics**: CPU, memory, response times
- **Health Checks**: Automatic monitoring
- **Alerts**: Email notifications for issues

### Auto-Deploy
- **GitHub Integration**: Auto-deploy on push
- **Build Logs**: Detailed build information
- **Rollback**: Easy rollback to previous versions

## 🔧 Configuration Files

### render.yaml (Auto-deployment)
```yaml
services:
  - type: web
    name: movie-site-search-api
    env: java
    buildCommand: mvn clean package -DskipTests
    startCommand: java -jar target/movie-site-search-api-1.0.0.jar
    plan: free
    healthCheckPath: /api/movie-sites/health
```

### application-render.yml
- Optimized for Render environment
- Port 10000 (Render default)
- CORS configured for Vercel
- H2 database for free tier

## 💰 Pricing

### Free Tier
- **750 hours/month** - Enough for testing
- **Sleeps after 15 minutes** of inactivity
- **Cold starts** - 30-60 seconds to wake up

### Paid Plans
- **$7/month** - Always on, no sleep
- **Better performance** - Faster response times
- **More resources** - Higher limits

## 🚨 Important Notes

### Free Tier Limitations
- **Service sleeps** after 15 minutes of inactivity
- **Cold start time** - First request after sleep takes 30-60 seconds
- **Monthly hours limit** - 750 hours (about 25 days)

### Solutions for Free Tier
1. **Keep-alive service** - Ping every 10 minutes
2. **Upgrade to paid** - $7/month for always-on
3. **Use multiple free services** - Rotate between them

## 🔄 Auto-Deploy Setup

### Option 1: render.yaml (Recommended)
1. Add `render.yaml` to your repository root
2. Push to GitHub
3. Render auto-detects and deploys

### Option 2: Manual Configuration
1. Configure through Render dashboard
2. Set up GitHub webhook
3. Auto-deploy on push

## 🛠️ Troubleshooting

### Build Failures
```bash
# Check build logs in Render dashboard
# Common issues:
# - Missing dependencies
# - Java version mismatch
# - Maven build errors
```

### Runtime Errors
```bash
# Check service logs
# Common issues:
# - Missing environment variables
# - Port configuration
# - Database connection
```

### Health Check Failures
```bash
# Verify health endpoint works
curl https://your-app.onrender.com/api/movie-sites/health

# Check application logs
# Verify port configuration
```

## 📈 Performance Optimization

### For Free Tier
- **Minimize cold starts** - Keep service warm
- **Optimize startup time** - Reduce dependencies
- **Use caching** - Reduce database calls

### For Paid Plans
- **Scale resources** - Increase CPU/memory
- **Use external database** - PostgreSQL addon
- **Enable monitoring** - Set up alerts

## 🔗 Complete System URLs

After deployment:
- **Frontend**: https://movie-agent-vercel-n9nz.vercel.app
- **Backend**: https://movie-site-search-api.onrender.com
- **Admin Dashboard**: https://movie-agent-vercel-n9nz.vercel.app/admin
- **API Docs**: https://movie-site-search-api.onrender.com/swagger-ui.html
- **n8n**: https://n8n-7j94.onrender.com

## ✅ Deployment Checklist

- [ ] Render account created
- [ ] GitHub repository connected
- [ ] Web service configured
- [ ] Environment variables set
- [ ] Service deployed successfully
- [ ] Health check passing
- [ ] Frontend updated with new API URL
- [ ] All endpoints tested
- [ ] n8n workflows updated
- [ ] Monitoring configured

## 🎉 Success!

Your backend is now deployed on Render! The system should be fully functional with:
- ✅ Automatic deployments from GitHub
- ✅ Built-in monitoring and health checks
- ✅ Easy environment variable management
- ✅ Free tier to get started

**Next**: Update your frontend to use the new Render backend URL and test the complete system!