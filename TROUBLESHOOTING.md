# 🔧 Troubleshooting Guide

## 🚨 Vercel Internal Server Error Fix

Your Vercel deployment at https://movie-agent-vercel-n9nz.vercel.app/ is showing an internal server error. Here's how to fix it:

### **Step 1: Check Environment Variables**

Go to your Vercel dashboard and ensure these environment variables are set:

```bash
NEXT_PUBLIC_API_URL=https://movie-site-search-api.fly.dev
NEXT_PUBLIC_N8N_URL=https://n8n-7j94.onrender.com
```

**How to set them:**
1. Go to [vercel.com/dashboard](https://vercel.com/dashboard)
2. Select your project: `movie-agent-vercel-n9nz`
3. Go to **Settings** → **Environment Variables**
4. Add each variable for **Production**, **Preview**, and **Development**

### **Step 2: Check Build Logs**

1. Go to your Vercel dashboard
2. Click on your project
3. Go to **Deployments** tab
4. Click on the failed deployment
5. Check the **Build Logs** for errors

### **Step 3: Force Redeploy**

After setting environment variables:

```bash
# Option 1: Redeploy from dashboard
# Go to Deployments → Click "Redeploy"

# Option 2: Push a small change to trigger redeploy
git commit --allow-empty -m "Trigger redeploy"
git push origin main
```

### **Step 4: Check Function Logs**

1. In Vercel dashboard
2. Go to **Functions** tab
3. Check for any runtime errors

## 🔍 Common Issues & Solutions

### **Issue 1: Missing Environment Variables**
**Error**: `process.env.NEXT_PUBLIC_API_URL is undefined`

**Solution**:
```bash
# Set in Vercel dashboard
NEXT_PUBLIC_API_URL=https://movie-site-search-api.fly.dev
NEXT_PUBLIC_N8N_URL=https://n8n-7j94.onrender.com
```

### **Issue 2: Build Errors**
**Error**: TypeScript or build compilation errors

**Solution**:
```bash
# Test build locally first
cd frontend
npm run build

# Fix any TypeScript errors
npm run lint
```

### **Issue 3: API Connection Issues**
**Error**: Cannot connect to backend API

**Solution**:
1. Ensure backend is deployed and running
2. Check CORS configuration
3. Verify API URL is correct

### **Issue 4: Import/Export Errors**
**Error**: Module import/export issues

**Solution**:
```bash
# Check all imports in your files
# Ensure proper default exports
```

## 🛠️ Quick Fixes

### **Fix 1: Update package.json**
Ensure your `frontend/package.json` has correct scripts:

```json
{
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint"
  }
}
```

### **Fix 2: Check next.config.js**
Ensure proper configuration (already updated in previous step)

### **Fix 3: Verify File Structure**
```
frontend/
├── app/
│   ├── admin/
│   │   └── page.tsx
│   ├── globals.css
│   ├── layout.tsx
│   ├── page.tsx
│   ├── error.tsx      # Added for error handling
│   ├── loading.tsx    # Added for loading states
│   └── not-found.tsx  # Added for 404 pages
├── package.json
├── next.config.js
└── tailwind.config.js
```

## 🧪 Testing Steps

### **Step 1: Test Locally**
```bash
cd frontend
npm install
npm run build
npm start
```

### **Step 2: Test Environment Variables**
```bash
# Create .env.local for testing
echo "NEXT_PUBLIC_API_URL=http://localhost:8080" > .env.local
echo "NEXT_PUBLIC_N8N_URL=https://n8n-7j94.onrender.com" >> .env.local

npm run dev
```

### **Step 3: Test Production Build**
```bash
npm run build
npm start
# Visit http://localhost:3000
```

## 📞 Immediate Action Plan

### **Right Now:**

1. **Set Environment Variables in Vercel**:
   - Go to Vercel dashboard
   - Settings → Environment Variables
   - Add `NEXT_PUBLIC_API_URL` and `NEXT_PUBLIC_N8N_URL`

2. **Redeploy**:
   - Go to Deployments
   - Click "Redeploy" on latest deployment

3. **Check Logs**:
   - Monitor build and function logs
   - Look for specific error messages

### **If Still Failing:**

1. **Clone and Test Locally**:
```bash
git clone your-repo
cd movie-site-search-system/frontend
npm install
npm run build
```

2. **Check for Specific Errors**:
   - TypeScript errors
   - Missing dependencies
   - Import/export issues

3. **Simplify and Redeploy**:
   - Comment out complex features temporarily
   - Deploy basic version first
   - Add features incrementally

## 🔧 Alternative Deployment

If Vercel continues to fail, try these alternatives:

### **Option 1: Netlify**
```bash
# Deploy to Netlify instead
npm run build
# Upload dist folder to Netlify
```

### **Option 2: GitHub Pages**
```bash
# Add to package.json
"homepage": "https://yourusername.github.io/movie-site-search-system"

# Deploy to GitHub Pages
npm run build
```

### **Option 3: Railway**
```bash
# Deploy frontend to Railway
# Similar to backend deployment
```

## 📊 Monitoring

Once fixed, monitor:
- **Vercel Analytics**: Performance metrics
- **Error Tracking**: Runtime errors
- **Build Times**: Deployment speed
- **Function Logs**: Server-side errors

## 🆘 Emergency Contact

If you need immediate help:
1. Check Vercel Status: https://vercel-status.com/
2. Vercel Discord: https://vercel.com/discord
3. GitHub Issues: Create issue in your repo

---

**Most likely fix**: Set the environment variables in Vercel dashboard and redeploy! 🚀