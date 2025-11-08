# Render Environment Variables Documentation

This document lists all required environment variables for deploying the Stock Monitoring microservices architecture on Render.

## Overview

Environment variables are set in the Render dashboard for each service. Some variables are automatically provided by Render's service linking, while others must be set manually.

## Common Variables (All Services)

### Required
- `SPRING_PROFILES_ACTIVE=render` - Activates Render-specific configuration profile
- `SPRING_OUTPUT_ANSI_ENABLED=ALWAYS` - Enables colored console output

## API Gateway

### Service Discovery URLs
- `AUTH_SERVICE_URL=auth-service.onrender.com` - Internal URL for auth service
- `AUTH_SERVICE_PORT=8081` - Port for auth service
- `PROFILE_SERVICE_URL=profile-service.onrender.com` - Internal URL for profile service
- `PROFILE_SERVICE_PORT=8082` - Port for profile service
- `MASTER_DATA_SERVICE_URL=master-data-service.onrender.com` - Internal URL for master data service
- `MASTER_DATA_SERVICE_PORT=8083` - Port for master data service

### Redis (Auto-linked from Render)
- `REDIS_HOST` - Automatically set when Redis service is linked
- `REDIS_PORT` - Automatically set when Redis service is linked
- `REDIS_PASSWORD` - Automatically set when Redis service is linked (if password protected)

### JWT
- `JWT_SECRET` - **REQUIRED** - Secret key for JWT token signing/validation (must be same across all services)

## Auth Service

### Database (Auto-linked from Render PostgreSQL)
- `SPRING_DATASOURCE_URL` - Automatically set when PostgreSQL is linked
- `SPRING_DATASOURCE_USERNAME` - Automatically set when PostgreSQL is linked
- `SPRING_DATASOURCE_PASSWORD` - Automatically set when PostgreSQL is linked

### Redis (Auto-linked from Render)
- `REDIS_HOST` - Automatically set when Redis service is linked
- `REDIS_PORT` - Automatically set when Redis service is linked
- `REDIS_PASSWORD` - Automatically set when Redis service is linked (if password protected)

### JWT
- `JWT_SECRET` - **REQUIRED** - Secret key for JWT token signing/validation (must match API Gateway)
- `JWT_EXPIRATION=86400000` - JWT token expiration in milliseconds (default: 24 hours)

### Email Configuration
- `MAIL_HOST=smtp.gmail.com` - SMTP server host
- `MAIL_PORT=587` - SMTP server port
- `MAIL_USERNAME` - **REQUIRED** - Email account username
- `MAIL_PASSWORD` - **REQUIRED** - Email account password or app-specific password

## Profile Service

### Database (Auto-linked from Render PostgreSQL)
- `SPRING_DATASOURCE_URL` - Automatically set when PostgreSQL is linked
- `SPRING_DATASOURCE_USERNAME` - Automatically set when PostgreSQL is linked
- `SPRING_DATASOURCE_PASSWORD` - Automatically set when PostgreSQL is linked

## Master Data Service

### Database (Auto-linked from Render PostgreSQL)
- `SPRING_DATASOURCE_URL` - Automatically set when PostgreSQL is linked
- `SPRING_DATASOURCE_USERNAME` - Automatically set when PostgreSQL is linked
- `SPRING_DATASOURCE_PASSWORD` - Automatically set when PostgreSQL is linked

## Setup Instructions

### Step 1: Create Infrastructure Services
1. Create PostgreSQL database in Render dashboard
   - Name: `stock-monitoring-db`
   - Plan: Free (90 days)
   - Note the connection details

2. Create Redis instance in Render dashboard
   - Name: `stock-monitoring-redis`
   - Plan: Free
   - Note the connection details

### Step 2: Deploy Services
For each service (API Gateway, Auth Service, Profile Service, Master Data Service):

1. **Link Services** (in Render dashboard):
   - Link PostgreSQL database (auto-sets DATASOURCE variables)
   - Link Redis instance (auto-sets REDIS variables)

2. **Set Manual Variables**:
   - `SPRING_PROFILES_ACTIVE=render`
   - `SPRING_OUTPUT_ANSI_ENABLED=ALWAYS`
   - `JWT_SECRET=<your-secret-key>` (same for all services)

3. **Service-Specific Variables**:
   - **API Gateway**: Service URLs and ports
   - **Auth Service**: `MAIL_USERNAME`, `MAIL_PASSWORD`, `JWT_EXPIRATION` (optional)

### Step 3: Verify Configuration
After deployment, verify:
- All services can connect to PostgreSQL
- All services can connect to Redis
- API Gateway can reach private services
- JWT_SECRET is consistent across services

## Security Notes

1. **Never commit secrets to Git** - All sensitive values should be set in Render dashboard only
2. **JWT_SECRET** - Use a strong, randomly generated secret (minimum 256 bits)
3. **Database Password** - Render auto-generates secure passwords
4. **Email Password** - Use app-specific password for Gmail, not your account password

## Generating JWT Secret

You can generate a secure JWT secret using:

```bash
# Using OpenSSL
openssl rand -base64 64

# Or using Java
java -cp . -c "System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(javax.crypto.KeyGenerator.getInstance(\"HmacSHA256\").generateKey().getEncoded()))"
```

## Environment Variable Priority

1. Render dashboard environment variables (highest priority)
2. `application-render.yml` file (with defaults)
3. `application.yml` file (base configuration)

## Troubleshooting

### Service can't connect to database
- Verify PostgreSQL service is linked in Render dashboard
- Check `SPRING_DATASOURCE_URL` is set correctly
- Ensure database is running and accessible

### Service can't connect to Redis
- Verify Redis service is linked in Render dashboard
- Check `REDIS_HOST` and `REDIS_PORT` are set
- Ensure Redis is running

### JWT validation fails
- Ensure `JWT_SECRET` is identical across API Gateway and Auth Service
- Verify secret is set correctly (no extra spaces or quotes)

### Services can't communicate
- Verify service URLs are correct (use Render internal DNS names)
- Check that private services are marked as `isPrivate: true`
- Ensure ports match the service configuration

