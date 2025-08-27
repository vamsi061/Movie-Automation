# 🚀 Deployment Guide

Complete guide for deploying the Movie Site Search System to production.

## 📋 Prerequisites

- GitHub account
- Vercel account (for frontend)
- Fly.io account (for backend)
- Browserless.io API key
- n8n instance (already running at https://n8n-7j94.onrender.com)

## 📂 Repository Setup

### 1. Create GitHub Repository

```bash
# Initialize git repository
git init

# Add all files
git add .

# Initial commit
git commit -m "Initial commit: Movie Site Search System"

# Add remote origin
git remote add origin https://github.com/yourusername/movie-site-search-system.git

# Push to GitHub
git branch -M main
git push -u origin main
```

### 2. Repository Structure
```
movie-site-search-system/
├── frontend/                 # Next.js frontend
├── src/                     # Spring Boot backend
├── n8n-workflows/           # Automation workflows
├── Dockerfile              # Backend container
├── fly.toml               # Fly.io config
├── pom.xml                # Maven config
├── .gitignore             # Git ignore rules
├── README.md              # Main documentation
└── DEPLOYMENT.md          # This file
```

## 🌐 Frontend Deployment (Vercel)

### 1. Connect GitHub to Vercel

1. Go to [vercel.com](https://vercel.com)
2. Sign in with GitHub
3. Click "New Project"
4. Import your repository
5. Select the `frontend` folder as root directory

### 2. Configure Build Settings

**Framework Preset**: Next.js
**Root Directory**: `frontend`
**Build Command**: `npm run build`
**Output Directory**: `.next`

### 3. Environment Variables

Add these in Vercel dashboard:

```bash
NEXT_PUBLIC_API_URL=https://movie-site-search-api.fly.dev
NEXT_PUBLIC_N8N_URL=https://n8n-7j94.onrender.com
```

### 4. Deploy

```bash
# Using Vercel CLI (alternative)
cd frontend
npx vercel --prod
```

**Frontend URL**: `https://your-project.vercel.app`

## 🚁 Backend Deployment (Fly.io)

### 1. Install Fly CLI

```bash
# macOS
brew install flyctl

# Linux/WSL
curl -L https://fly.io/install.sh | sh

# Windows
iwr https://fly.io/install.ps1 -useb | iex
```

### 2. Login and Create App

```bash
# Login to Fly.io
flyctl auth login

# Create new app
flyctl apps create movie-site-search-api

# Verify app creation
flyctl apps list
```

### 3. Set Environment Variables

```bash
# Required secrets
flyctl secrets set BROWSERLESS_API_KEY=your_browserless_api_key_here
flyctl secrets set BROWSERLESS_URL=https://chrome.browserless.io
flyctl secrets set N8N_WEBHOOK_URL=https://n8n-7j94.onrender.com

# Optional notification secrets
flyctl secrets set TELEGRAM_BOT_TOKEN=your_telegram_bot_token
flyctl secrets set TELEGRAM_CHAT_ID=your_telegram_chat_id
flyctl secrets set SLACK_WEBHOOK_URL=your_slack_webhook_url
flyctl secrets set DISCORD_WEBHOOK_URL=your_discord_webhook_url

# Verify secrets
flyctl secrets list
```

### 4. Deploy Backend

```bash
# Deploy from project root
flyctl deploy

# Check deployment status
flyctl status

# View logs
flyctl logs

# Check app info
flyctl info
```

**Backend URL**: `https://movie-site-search-api.fly.dev`

### 5. Verify Deployment

```bash
# Test health endpoint
curl https://movie-site-search-api.fly.dev/api/movie-sites/health

# Test search endpoint
curl https://movie-site-search-api.fly.dev/api/movie-sites/search/movierulz
```

## 🔄 n8n Workflow Setup

### 1. Access n8n Instance

Visit: https://n8n-7j94.onrender.com

### 2. Import Workflows

1. Go to **Workflows** → **Import from file**
2. Upload `n8n-workflows/monitoring-workflows.json`
3. Configure each workflow individually

### 3. Set Environment Variables

In n8n Settings → Environment Variables:

```bash
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_CHAT_ID=your_chat_id
SLACK_WEBHOOK_URL=your_slack_webhook
ADMIN_EMAIL=admin@yourcompany.com
```

### 4. Enable Workflows

1. **Daily Site Health Check** - Enable schedule
2. **Site Down Alert Workflow** - Activate webhook
3. **Weekly Summary Report** - Enable schedule
4. **Auto Site Discovery** - Enable schedule

### 5. Test Workflows

```bash
# Test monitoring webhook
curl -X POST https://n8n-7j94.onrender.com/webhook/monitoring \
  -H "Content-Type: application/json" \
  -d '{"action": "test", "message": "Test notification"}'
```

## 🔧 Post-Deployment Configuration

### 1. Update Frontend Environment

Update Vercel environment variables with actual backend URL:

```bash
NEXT_PUBLIC_API_URL=https://movie-site-search-api.fly.dev
```

Redeploy frontend after updating environment variables.

### 2. Configure CORS

Update `application-production.yml` with your actual Vercel domain:

```yaml
cors:
  allowed-origins: 
    - https://your-actual-project.vercel.app
    - https://*.vercel.app
    - http://localhost:3000
```

### 3. Test Complete System

1. **Frontend**: Visit your Vercel URL
2. **Search**: Test single and batch search
3. **Admin**: Access `/admin` dashboard
4. **API**: Test all endpoints
5. **Monitoring**: Verify n8n workflows

## 📊 Monitoring Setup

### 1. Telegram Bot Setup

1. Message @BotFather on Telegram
2. Create new bot: `/newbot`
3. Get bot token
4. Add bot to your group/channel
5. Get chat ID using: `https://api.telegram.org/bot<TOKEN>/getUpdates`

### 2. Slack Integration

1. Create Slack app at api.slack.com
2. Enable Incoming Webhooks
3. Create webhook URL
4. Add to workspace

### 3. Test Notifications

```bash
# Test from backend
curl -X POST https://movie-site-search-api.fly.dev/api/admin/notifications/test
```

## 🔒 Security Checklist

- [ ] All API keys stored as secrets
- [ ] CORS properly configured
- [ ] Environment variables not in code
- [ ] HTTPS enabled on all endpoints
- [ ] Database credentials secured
- [ ] Admin endpoints protected

## 📈 Performance Optimization

### 1. Backend Scaling

```bash
# Scale Fly.io app
flyctl scale count 2

# Monitor performance
flyctl metrics
```

### 2. Frontend Optimization

- Enable Vercel Analytics
- Configure caching headers
- Optimize images and assets
- Monitor Core Web Vitals

## 🧪 Testing Production

### 1. API Testing

```bash
# Health check
curl https://movie-site-search-api.fly.dev/api/movie-sites/health

# Search test
curl https://movie-site-search-api.fly.dev/api/movie-sites/search/movierulz

# Admin dashboard
curl https://movie-site-search-api.fly.dev/api/admin/dashboard

# Batch search
curl -X POST https://movie-site-search-api.fly.dev/api/movie-sites/search/batch \
  -H "Content-Type: application/json" \
  -d '{"siteNames": ["movierulz", "moviezap"]}'
```

### 2. Frontend Testing

1. Visit your Vercel URL
2. Test search functionality
3. Check admin dashboard
4. Verify responsive design
5. Test error handling

### 3. Integration Testing

1. Search from frontend
2. Check admin dashboard updates
3. Verify n8n workflow triggers
4. Test notification delivery

## 🚨 Troubleshooting

### Common Issues

**Backend not starting:**
```bash
flyctl logs
flyctl status
```

**Frontend build errors:**
```bash
# Check Vercel build logs
# Verify environment variables
```

**API CORS errors:**
```bash
# Update CORS configuration
# Redeploy backend
```

**n8n workflows not triggering:**
```bash
# Check webhook URLs
# Verify environment variables
# Test workflows manually
```

## 📞 Support

- **Fly.io Docs**: https://fly.io/docs/
- **Vercel Docs**: https://vercel.com/docs
- **n8n Docs**: https://docs.n8n.io/

## 🎉 Go Live Checklist

- [ ] Repository pushed to GitHub
- [ ] Frontend deployed to Vercel
- [ ] Backend deployed to Fly.io
- [ ] Environment variables configured
- [ ] n8n workflows imported and enabled
- [ ] Notifications configured and tested
- [ ] CORS properly set up
- [ ] All endpoints tested
- [ ] Admin dashboard accessible
- [ ] Monitoring alerts working

**🚀 Your Movie Site Search System is now live!**

- **Frontend**: https://your-project.vercel.app
- **Admin Dashboard**: https://your-project.vercel.app/admin
- **Backend API**: https://movie-site-search-api.fly.dev
- **API Docs**: https://movie-site-search-api.fly.dev/swagger-ui.html