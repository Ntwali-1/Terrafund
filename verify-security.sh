#!/bin/bash

# Security Verification Script
# This script checks if all sensitive data has been properly secured

echo "🔍 Security Verification Script"
echo "================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0
WARNINGS=0

# Check 1: Verify .env is not tracked by Git
echo "1. Checking if .env is tracked by Git..."
if git ls-files --error-unmatch .env 2>/dev/null; then
    echo -e "${RED}❌ FAIL: .env file is tracked by Git!${NC}"
    echo "   Run: git rm --cached .env"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ PASS: .env is not tracked by Git${NC}"
fi
echo ""

# Check 2: Verify .env.example exists
echo "2. Checking if .env.example exists..."
if [ -f ".env.example" ]; then
    echo -e "${GREEN}✅ PASS: .env.example exists${NC}"
else
    echo -e "${RED}❌ FAIL: .env.example not found${NC}"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check 3: Verify .gitignore includes .env
echo "3. Checking if .gitignore includes .env..."
if grep -q "^\.env$" .gitignore || grep -q "^\*\.env$" .gitignore; then
    echo -e "${GREEN}✅ PASS: .gitignore includes .env${NC}"
else
    echo -e "${RED}❌ FAIL: .env not found in .gitignore${NC}"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check 4: Search for hardcoded JWT secret
echo "4. Checking for hardcoded JWT secrets..."
OLD_JWT="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
if grep -r "$OLD_JWT" --include="*.yml" --include="*.yaml" --include="*.properties" . 2>/dev/null | grep -v ".git" | grep -v "target"; then
    echo -e "${RED}❌ FAIL: Found hardcoded JWT secret!${NC}"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ PASS: No hardcoded JWT secrets found${NC}"
fi
echo ""

# Check 5: Search for hardcoded Cloudinary credentials
echo "5. Checking for hardcoded Cloudinary credentials..."
if grep -r "dwzy8ynvp\|697423964567684\|j_ara_l1OAlEbN7vFXy8DMCCaj0" --include="*.yml" --include="*.yaml" . 2>/dev/null | grep -v ".git" | grep -v "target"; then
    echo -e "${RED}❌ FAIL: Found hardcoded Cloudinary credentials!${NC}"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ PASS: No hardcoded Cloudinary credentials found${NC}"
fi
echo ""

# Check 6: Verify environment variables are used in config files
echo "6. Checking if environment variables are used in config files..."
if grep -r "\${JWT_SECRET}" config-server/src/main/resources/config/*.yml >/dev/null 2>&1; then
    echo -e "${GREEN}✅ PASS: JWT_SECRET environment variable is used${NC}"
else
    echo -e "${YELLOW}⚠️  WARNING: JWT_SECRET environment variable not found in config files${NC}"
    WARNINGS=$((WARNINGS + 1))
fi
echo ""

# Check 7: Verify .env file exists (warning only)
echo "7. Checking if .env file exists..."
if [ -f ".env" ]; then
    echo -e "${GREEN}✅ PASS: .env file exists${NC}"
else
    echo -e "${YELLOW}⚠️  WARNING: .env file not found. Copy from .env.example${NC}"
    WARNINGS=$((WARNINGS + 1))
fi
echo ""

# Check 8: Search for common password patterns
echo "8. Checking for common hardcoded passwords..."
if grep -r "password.*=.*postgres\|password.*=.*admin\|password.*=.*123" --include="*.yml" --include="*.yaml" . 2>/dev/null | grep -v ".git" | grep -v "target" | grep -v ".env.example" | grep -v "GUIDE.md"; then
    echo -e "${RED}❌ FAIL: Found potential hardcoded passwords!${NC}"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ PASS: No obvious hardcoded passwords found${NC}"
fi
echo ""

# Check 9: Verify docker-compose uses environment variables
echo "9. Checking if docker-compose.yml uses environment variables..."
if grep -q "\${POSTGRES_PASSWORD}" docker-compose.yml && grep -q "\${JWT_SECRET}" docker-compose.yml; then
    echo -e "${GREEN}✅ PASS: docker-compose.yml uses environment variables${NC}"
else
    echo -e "${RED}❌ FAIL: docker-compose.yml may have hardcoded values${NC}"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Summary
echo "================================"
echo "📊 Summary"
echo "================================"
if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}🎉 All checks passed! Your secrets are secure.${NC}"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠️  $WARNINGS warning(s) found. Review above.${NC}"
    exit 0
else
    echo -e "${RED}❌ $ERRORS error(s) and $WARNINGS warning(s) found.${NC}"
    echo -e "${RED}Please fix the errors before committing!${NC}"
    exit 1
fi
