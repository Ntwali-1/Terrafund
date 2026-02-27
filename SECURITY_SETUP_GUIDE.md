# Security Setup Guide - Environment Variables

## ✅ What Has Been Done

All sensitive credentials have been removed from configuration files and replaced with environment variables. This ensures:
- No secrets are exposed in your Git repository
- Easy configuration across different environments (dev, staging, production)
- Better security practices

---

## 🔒 Secured Variables

The following sensitive information has been moved to environment variables:

### Database Credentials
- `POSTGRES_USER` - PostgreSQL username
- `POSTGRES_PASSWORD` - PostgreSQL password
- `SPRING_DATASOURCE_URL` - User service database URL
- `SPRING_DATASOURCE_USERNAME` - User service database username
- `SPRING_DATASOURCE_PASSWORD` - User service database password
- `LAND_DATASOURCE_URL` - Land service database URL
- `LAND_DATASOURCE_USERNAME` - Land service database username
- `LAND_DATASOURCE_PASSWORD` - Land service database password
- `INVESTMENT_DATASOURCE_URL` - Investment service database URL
- `INVESTMENT_DATASOURCE_USERNAME` - Investment service database username
- `INVESTMENT_DATASOURCE_PASSWORD` - Investment service database password

### JWT Configuration
- `JWT_SECRET` - Secret key for JWT token signing (MUST be same across all services)
- `JWT_EXPIRATION` - JWT token expiration time in milliseconds

### Cloudinary Configuration
- `CLOUDINARY_CLOUD_NAME` - Your Cloudinary cloud name
- `CLOUDINARY_API_KEY` - Your Cloudinary API key
- `CLOUDINARY_API_SECRET` - Your Cloudinary API secret

### PgAdmin Configuration
- `PGADMIN_DEFAULT_EMAIL` - PgAdmin login email
- `PGADMIN_DEFAULT_PASSWORD` - PgAdmin login password

---

## 📋 Setup Instructions

### Step 1: Copy the Template

```bash
cd microservices-springboot
cp .env.example .env
```

### Step 2: Generate Secure JWT Secret

Generate a secure JWT secret key (minimum 256 bits):

**Using OpenSSL (Linux/Mac/Git Bash):**
```bash
openssl rand -base64 32
```

**Using PowerShell (Windows):**
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

**Using Online Tool:**
- Go to https://generate-secret.vercel.app/32
- Copy the generated secret

### Step 3: Edit .env File

Open `microservices-springboot/.env` and fill in your actual values:

```env
# Database Configuration
POSTGRES_USER=postgres
POSTGRES_PASSWORD=YourSecurePassword123!
POSTGRES_DB=postgres

# User Service Database
SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/userdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=YourSecurePassword123!

# Land Service Database
LAND_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/landdb
LAND_DATASOURCE_USERNAME=postgres
LAND_DATASOURCE_PASSWORD=YourSecurePassword123!

# Investment Service Database
INVESTMENT_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/investmentdb
INVESTMENT_DATASOURCE_USERNAME=postgres
INVESTMENT_DATASOURCE_PASSWORD=YourSecurePassword123!

# JWT Configuration (MUST be same across all services)
JWT_SECRET=your_generated_jwt_secret_from_step_2
JWT_EXPIRATION=86400000

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret

# PgAdmin Configuration
PGADMIN_DEFAULT_EMAIL=admin@yourcompany.com
PGADMIN_DEFAULT_PASSWORD=YourPgAdminPassword123!
```

### Step 4: Verify .gitignore

The `.gitignore` file already includes `.env` files, so they won't be committed:

```gitignore
# Docker
*.env
.env
```

---

## 🚀 Running the Application

### With Docker Compose

Docker Compose will automatically load variables from the `.env` file:

```bash
cd microservices-springboot
docker-compose up -d
```

### Local Development (Without Docker)

For local development, you need to set environment variables before running each service.

**Option 1: Set in IDE (IntelliJ IDEA)**
1. Go to Run → Edit Configurations
2. Select your Spring Boot application
3. Add environment variables in the "Environment variables" field
4. Format: `JWT_SECRET=your_secret;SPRING_DATASOURCE_PASSWORD=your_password`

**Option 2: Set in Terminal (Linux/Mac)**
```bash
export JWT_SECRET=your_jwt_secret
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/userdb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password
# ... set other variables

# Then run the service
cd user-service
mvn spring-boot:run
```

**Option 3: Set in PowerShell (Windows)**
```powershell
$env:JWT_SECRET="your_jwt_secret"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/userdb"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="your_password"
# ... set other variables

# Then run the service
cd user-service
mvn spring-boot:run
```

**Option 4: Create a startup script**

Create `microservices-springboot/run-local.sh` (Linux/Mac):
```bash
#!/bin/bash
export JWT_SECRET=your_jwt_secret
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/userdb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password
# ... other variables

cd user-service
mvn spring-boot:run
```

Create `microservices-springboot/run-local.ps1` (Windows):
```powershell
$env:JWT_SECRET="your_jwt_secret"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/userdb"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="your_password"
# ... other variables

cd user-service
mvn spring-boot:run
```

---

## 🔐 Security Best Practices

### 1. Never Commit .env Files
✅ `.env` is already in `.gitignore`
✅ Always use `.env.example` as a template
❌ Never commit actual credentials

### 2. Use Strong Passwords
- Minimum 12 characters
- Mix of uppercase, lowercase, numbers, and special characters
- Use a password manager to generate and store passwords

### 3. Rotate Secrets Regularly
- Change JWT secrets periodically
- Update database passwords regularly
- Rotate API keys when team members leave

### 4. Different Secrets Per Environment
- Use different secrets for dev, staging, and production
- Never use production secrets in development

### 5. Limit Access
- Only share credentials with team members who need them
- Use secret management tools (AWS Secrets Manager, HashiCorp Vault) for production

---

## 🌍 Environment-Specific Configuration

### Development (.env)
```env
JWT_SECRET=dev_secret_key_not_for_production
POSTGRES_PASSWORD=dev_password
```

### Production (.env.production)
```env
JWT_SECRET=super_secure_production_secret_256_bits
POSTGRES_PASSWORD=VerySecureProductionPassword!@#
```

Load production environment:
```bash
docker-compose --env-file .env.production up -d
```

---

## 🧪 Testing Configuration

Verify environment variables are loaded:

```bash
# Check if .env file exists
ls -la .env

# Test with docker-compose
docker-compose config

# Check environment variables in running container
docker exec user-service env | grep JWT_SECRET
```

---

## 🆘 Troubleshooting

### Problem: Services can't connect to database
**Solution:** Check that database URLs and credentials in `.env` match your PostgreSQL setup

### Problem: JWT authentication fails
**Solution:** Ensure `JWT_SECRET` is the same across all services (user-service, land-service, investment-service, api-gateway)

### Problem: Cloudinary uploads fail
**Solution:** Verify your Cloudinary credentials are correct in `.env`

### Problem: Environment variables not loading
**Solution:** 
- Ensure `.env` file is in the same directory as `docker-compose.yml`
- Check for syntax errors in `.env` (no spaces around `=`)
- Restart Docker containers: `docker-compose down && docker-compose up -d`

---

## 📝 Checklist Before Committing

- [ ] `.env` file is NOT in Git (check with `git status`)
- [ ] `.env.example` is updated with new variables (without actual values)
- [ ] `.gitignore` includes `*.env` and `.env`
- [ ] All hardcoded secrets removed from YAML files
- [ ] README updated with setup instructions
- [ ] Team members notified about new environment variables

---

## 🔄 Sharing with Team Members

When a new team member joins:

1. Share the `.env.example` file (it's in Git)
2. Provide actual credentials through a secure channel (encrypted email, password manager, Slack DM)
3. Ask them to create their own `.env` file
4. Verify their setup works

**Never share credentials through:**
- Public Slack channels
- Email to multiple people
- Screenshots
- Unencrypted files

---

## 📚 Additional Resources

- [12-Factor App - Config](https://12factor.net/config)
- [OWASP - Secrets Management](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
- [Docker Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [Spring Boot External Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

---

## ✅ Summary

All sensitive information has been secured:
- ✅ Database credentials moved to environment variables
- ✅ JWT secrets externalized
- ✅ Cloudinary API keys secured
- ✅ PgAdmin credentials protected
- ✅ `.env` file added to `.gitignore`
- ✅ `.env.example` template created
- ✅ All configuration files updated

Your secrets are now safe from being exposed on GitHub! 🎉
