# Quick Start Guide

## 🚀 Get Started in 3 Steps

### Step 1: Setup Environment Variables

```bash
# Copy the template
cp .env.example .env

# Generate JWT secret
openssl rand -base64 32

# Edit .env and add your values
nano .env  # or use any text editor
```

### Step 2: Fill in Required Values

Edit `.env` file with your actual credentials:

```env
# Required - Generate using: openssl rand -base64 32
JWT_SECRET=your_generated_secret_here

# Required - Your database password
POSTGRES_PASSWORD=your_secure_password

# Required - Get from https://cloudinary.com/console
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Optional - Change if needed
PGADMIN_DEFAULT_EMAIL=admin@yourcompany.com
PGADMIN_DEFAULT_PASSWORD=your_pgadmin_password
```

### Step 3: Run the Application

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

---

## 🔗 Access Points

Once running, access your services at:

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081
- **Land Service**: http://localhost:8082
- **Investment Service**: http://localhost:8083
- **File Storage Service**: http://localhost:8084
- **File Storage Swagger**: http://localhost:8084/swagger-ui.html
- **PgAdmin**: http://localhost:5050

---

## ⚠️ Important Security Notes

1. **Never commit `.env` file** - It contains your secrets!
2. **Use strong passwords** - Minimum 12 characters
3. **Same JWT_SECRET** - Must be identical across all services
4. **Keep `.env.example` updated** - But without actual values

---

## 🆘 Common Issues

**Services won't start?**
```bash
# Check if .env file exists
ls -la .env

# Verify environment variables
docker-compose config
```

**JWT authentication fails?**
- Ensure `JWT_SECRET` is the same in all services
- Check that the secret is properly set in `.env`

**Database connection fails?**
- Verify `POSTGRES_PASSWORD` matches in all database URLs
- Check PostgreSQL is running: `docker-compose ps postgres`

---

## 📖 Full Documentation

- **Security Setup**: See `SECURITY_SETUP_GUIDE.md`
- **File Storage**: See `FILE_STORAGE_SERVICE_SETUP_GUIDE.md`

---

## 🎉 You're All Set!

Your microservices are now running with secure environment variables.
