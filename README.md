# 🎬 Movie Site Search System

A comprehensive full-stack application that automatically finds working links for movie streaming sites like Movierulz, Moviezap, and others using advanced web scraping with human-like behavior.

## 🏗️ Architecture

```
🌍 User / Client
    │
    ▼
Frontend (Next.js)
 Vercel Deployment
    │
    ▼
Backend API (Spring Boot)
 Fly.io Deployment
    │
    ▼
Browserless.io (SaaS)
 ↳ Runs Puppeteer/Playwright
 ↳ Handles redirects, ads, JS
 ↳ Returns clean page data
    │
    ▼
n8n Automation
 Render Deployment
 ↳ Monitoring workflows
 ↳ Alert notifications
 ↳ Scheduled tasks
```

## 🚀 Features

### Core Functionality
- **Human-like Search**: Stealth mode Puppeteer with realistic behavior
- **Multi-engine Search**: Google and DuckDuckGo integration
- **Real-time Results**: Live search with progress indicators
- **Batch Processing**: Search multiple sites simultaneously
- **URL Validation**: Automatic accessibility checking

### Monitoring & Alerting
- **Automated Health Checks**: Scheduled monitoring every 6 hours
- **Multi-channel Alerts**: Telegram, Slack, Discord notifications
- **System Metrics**: Uptime, response times, status tracking
- **Historical Data**: Trend analysis and reporting

### Admin Dashboard
- **Real-time Overview**: System health and metrics
- **Site Management**: Add, edit, delete, refresh sites
- **Monitoring Controls**: Manual health checks and alerts
- **Activity Logs**: Comprehensive audit trails

## 📋 Supported Movie Sites

- **Movierulz** - Popular movie streaming site
- **Moviezap** - Movie download and streaming platform
- **Tamilrockers** - Tamil and regional movies
- **Filmywap** - Bollywood and Hollywood movies
- **Worldfree4u** - Free movie downloads
- **9xmovies** - Movie streaming site
- **Khatrimaza** - Latest movies
- **Bolly4u** - Bollywood movies

## 🛠️ Tech Stack

### Frontend
- **Next.js 14** with TypeScript
- **Tailwind CSS** for styling
- **React Hooks** for state management
- **Axios** for API calls
- **React Hot Toast** for notifications

### Backend
- **Spring Boot 3.2** with Java 17
- **Spring Data JPA** for database operations
- **H2/MySQL** database support
- **RestTemplate** for HTTP requests
- **Scheduled Tasks** for automation

### Infrastructure
- **Vercel** - Frontend hosting
- **Fly.io** - Backend deployment
- **Browserless.io** - Web scraping service
- **n8n** - Workflow automation
- **Docker** - Containerization

## 📁 Project Structure

```
movie-site-search-system/
├── frontend/                 # Next.js frontend application
│   ├── app/                 # App router pages
│   ├── components/          # Reusable components
│   ├── public/             # Static assets
│   └── package.json        # Frontend dependencies
├── src/                    # Spring Boot backend
│   ├── main/java/com/movielinks/
│   │   ├── controller/     # REST controllers
│   │   ├── service/        # Business logic
│   │   ├── model/          # Entity models
│   │   └── repository/     # Data access layer
│   └── main/resources/     # Configuration files
├── n8n-workflows/          # Automation workflows
├── Dockerfile             # Backend containerization
├── fly.toml              # Fly.io configuration
├── pom.xml               # Maven dependencies
└── README.md             # This file
```

## 🚀 Quick Start

### Prerequisites
- **Java 17+**
- **Node.js 18+**
- **Maven 3.6+**
- **Browserless.io API key**

### Backend Setup
```bash
# Clone the repository
git clone <repository-url>
cd movie-site-search-system

# Set environment variables
export BROWSERLESS_API_KEY=your_api_key
export BROWSERLESS_URL=https://chrome.browserless.io

# Build and run backend
mvn clean install
mvn spring-boot:run
```

### Frontend Setup
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Set environment variables
echo "NEXT_PUBLIC_API_URL=http://localhost:8080" > .env.local

# Run development server
npm run dev
```

## 🌐 API Endpoints

### Public Endpoints
```bash
# Health check
GET /api/movie-sites/health

# Search single site
GET /api/movie-sites/search/{siteName}

# Search multiple sites
POST /api/movie-sites/search/batch
{
  "siteNames": ["movierulz", "moviezap"]
}

# Search all popular sites
GET /api/movie-sites/search/all

# Get supported sites
GET /api/movie-sites/supported
```

### Admin Endpoints
```bash
# Admin dashboard
GET /api/admin/dashboard

# Manage sites
GET /api/admin/sites
POST /api/admin/sites
PUT /api/admin/sites/{id}
DELETE /api/admin/sites/{id}

# Monitoring
GET /api/admin/monitoring/stats
POST /api/admin/monitoring/health-check

# Activity logs
GET /api/admin/logs
```

## 🔧 Configuration

### Environment Variables

#### Backend (Spring Boot)
```bash
BROWSERLESS_API_KEY=your_browserless_api_key
BROWSERLESS_URL=https://chrome.browserless.io
N8N_WEBHOOK_URL=https://n8n-7j94.onrender.com
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_CHAT_ID=your_chat_id
SLACK_WEBHOOK_URL=your_slack_webhook
```

#### Frontend (Next.js)
```bash
NEXT_PUBLIC_API_URL=https://movie-site-search-api.fly.dev
NEXT_PUBLIC_N8N_URL=https://n8n-7j94.onrender.com
```

## 🚀 Deployment

### Backend to Fly.io
```bash
# Install Fly CLI
curl -L https://fly.io/install.sh | sh

# Login and create app
flyctl auth login
flyctl apps create movie-site-search-api

# Set secrets
flyctl secrets set BROWSERLESS_API_KEY=your_key

# Deploy
flyctl deploy
```

### Frontend to Vercel
```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
cd frontend
vercel --prod
```

### n8n Workflows
1. Access your n8n instance at https://n8n-7j94.onrender.com
2. Import workflows from `n8n-workflows/monitoring-workflows.json`
3. Configure environment variables
4. Enable workflow schedules

## 📊 Monitoring

### Health Metrics
- **System Status**: HEALTHY, DEGRADED, CRITICAL
- **Uptime Percentage**: Overall system availability
- **Response Times**: Average API response times
- **Site Status**: Individual site health tracking

### Alerting Channels
- **Telegram**: Instant mobile notifications
- **Slack**: Team collaboration alerts
- **Discord**: Community notifications
- **Email**: Weekly summary reports

### Automated Workflows
- **Daily Health Checks**: Comprehensive site monitoring
- **Immediate Alerts**: Site down notifications
- **Weekly Reports**: Trend analysis and summaries
- **Auto Discovery**: Find new movie sites

## 🔒 Security Features

- **Stealth Mode**: Puppeteer with anti-detection
- **Rate Limiting**: Built-in request throttling
- **URL Validation**: Security checks before access
- **Error Handling**: Comprehensive error management
- **CORS Protection**: Secure cross-origin requests

## 🧪 Testing

### Backend Tests
```bash
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

### API Testing
```bash
# Test health endpoint
curl https://movie-site-search-api.fly.dev/api/movie-sites/health

# Test search
curl https://movie-site-search-api.fly.dev/api/movie-sites/search/movierulz
```

## 📈 Performance

- **Response Time**: < 2 seconds for single site search
- **Batch Processing**: 5-10 sites in parallel
- **Uptime Target**: 99.5% availability
- **Scalability**: Horizontal scaling on Fly.io

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: Check the `/docs` folder for detailed guides
- **Issues**: Report bugs via GitHub Issues
- **Discussions**: Use GitHub Discussions for questions

## 🔗 Links

- **Live Demo**: [Frontend URL]
- **API Documentation**: [Backend URL]/swagger-ui.html
- **Admin Dashboard**: [Frontend URL]/admin
- **n8n Workflows**: https://n8n-7j94.onrender.com

---

**Built with ❤️ for the movie community**