# Security Verification Script (PowerShell)
# This script checks if all sensitive data has been properly secured

Write-Host "🔍 Security Verification Script" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

$Errors = 0
$Warnings = 0

# Check 1: Verify .env is not tracked by Git
Write-Host "1. Checking if .env is tracked by Git..."
$gitTracked = git ls-files --error-unmatch .env 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "❌ FAIL: .env file is tracked by Git!" -ForegroundColor Red
    Write-Host "   Run: git rm --cached .env" -ForegroundColor Yellow
    $Errors++
} else {
    Write-Host "✅ PASS: .env is not tracked by Git" -ForegroundColor Green
}
Write-Host ""

# Check 2: Verify .env.example exists
Write-Host "2. Checking if .env.example exists..."
if (Test-Path ".env.example") {
    Write-Host "✅ PASS: .env.example exists" -ForegroundColor Green
} else {
    Write-Host "❌ FAIL: .env.example not found" -ForegroundColor Red
    $Errors++
}
Write-Host ""

# Check 3: Verify .gitignore includes .env
Write-Host "3. Checking if .gitignore includes .env..."
$gitignoreContent = Get-Content .gitignore -Raw
if ($gitignoreContent -match "\.env" -or $gitignoreContent -match "\*\.env") {
    Write-Host "✅ PASS: .gitignore includes .env" -ForegroundColor Green
} else {
    Write-Host "❌ FAIL: .env not found in .gitignore" -ForegroundColor Red
    $Errors++
}
Write-Host ""

# Check 4: Search for hardcoded JWT secret
Write-Host "4. Checking for hardcoded JWT secrets..."
$oldJWT = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
$jwtFound = Get-ChildItem -Recurse -Include *.yml,*.yaml,*.properties | 
    Where-Object { $_.FullName -notmatch "\.git|target" } |
    Select-String -Pattern $oldJWT -Quiet
if ($jwtFound) {
    Write-Host "❌ FAIL: Found hardcoded JWT secret!" -ForegroundColor Red
    $Errors++
} else {
    Write-Host "✅ PASS: No hardcoded JWT secrets found" -ForegroundColor Green
}
Write-Host ""

# Check 5: Search for hardcoded Cloudinary credentials
Write-Host "5. Checking for hardcoded Cloudinary credentials..."
$cloudinaryPatterns = "dwzy8ynvp|697423964567684|j_ara_l1OAlEbN7vFXy8DMCCaj0"
$cloudinaryFound = Get-ChildItem -Recurse -Include *.yml,*.yaml | 
    Where-Object { $_.FullName -notmatch "\.git|target" } |
    Select-String -Pattern $cloudinaryPatterns -Quiet
if ($cloudinaryFound) {
    Write-Host "❌ FAIL: Found hardcoded Cloudinary credentials!" -ForegroundColor Red
    $Errors++
} else {
    Write-Host "✅ PASS: No hardcoded Cloudinary credentials found" -ForegroundColor Green
}
Write-Host ""

# Check 6: Verify environment variables are used in config files
Write-Host "6. Checking if environment variables are used in config files..."
$configFiles = Get-ChildItem -Path "config-server/src/main/resources/config" -Filter *.yml -ErrorAction SilentlyContinue
$jwtEnvFound = $false
foreach ($file in $configFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match '\$\{JWT_SECRET\}') {
        $jwtEnvFound = $true
        break
    }
}
if ($jwtEnvFound) {
    Write-Host "✅ PASS: JWT_SECRET environment variable is used" -ForegroundColor Green
} else {
    Write-Host "⚠️  WARNING: JWT_SECRET environment variable not found in config files" -ForegroundColor Yellow
    $Warnings++
}
Write-Host ""

# Check 7: Verify .env file exists (warning only)
Write-Host "7. Checking if .env file exists..."
if (Test-Path ".env") {
    Write-Host "✅ PASS: .env file exists" -ForegroundColor Green
} else {
    Write-Host "⚠️  WARNING: .env file not found. Copy from .env.example" -ForegroundColor Yellow
    $Warnings++
}
Write-Host ""

# Check 8: Search for common password patterns
Write-Host "8. Checking for common hardcoded passwords..."
$passwordPatterns = "password.*=.*postgres|password.*=.*admin|password.*=.*123"
$passwordFound = Get-ChildItem -Recurse -Include *.yml,*.yaml | 
    Where-Object { $_.FullName -notmatch "\.git|target|\.env\.example|GUIDE\.md" } |
    Select-String -Pattern $passwordPatterns -Quiet
if ($passwordFound) {
    Write-Host "❌ FAIL: Found potential hardcoded passwords!" -ForegroundColor Red
    $Errors++
} else {
    Write-Host "✅ PASS: No obvious hardcoded passwords found" -ForegroundColor Green
}
Write-Host ""

# Check 9: Verify docker-compose uses environment variables
Write-Host "9. Checking if docker-compose.yml uses environment variables..."
$dockerComposeContent = Get-Content "docker-compose.yml" -Raw
if ($dockerComposeContent -match '\$\{POSTGRES_PASSWORD\}' -and $dockerComposeContent -match '\$\{JWT_SECRET\}') {
    Write-Host "✅ PASS: docker-compose.yml uses environment variables" -ForegroundColor Green
} else {
    Write-Host "❌ FAIL: docker-compose.yml may have hardcoded values" -ForegroundColor Red
    $Errors++
}
Write-Host ""

# Summary
Write-Host "================================" -ForegroundColor Cyan
Write-Host "📊 Summary" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
if ($Errors -eq 0 -and $Warnings -eq 0) {
    Write-Host "🎉 All checks passed! Your secrets are secure." -ForegroundColor Green
    exit 0
} elseif ($Errors -eq 0) {
    Write-Host "⚠️  $Warnings warning(s) found. Review above." -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "❌ $Errors error(s) and $Warnings warning(s) found." -ForegroundColor Red
    Write-Host "Please fix the errors before committing!" -ForegroundColor Red
    exit 1
}
