$ErrorActionPreference = "Continue"

$projectRoot = $PSScriptRoot
$backendDir = Join-Path $projectRoot "tripnest-backend"
$mavenHome = Join-Path $env:USERPROFILE ".m2"

# Explicitly use the current user's Maven cache and disable DevTools file-watcher restarts.
$env:MAVEN_USER_HOME = $mavenHome
$env:MAVEN_OPTS = "-Duser.home=$env:USERPROFILE -Dspring.devtools.restart.enabled=false"

Set-Location $backendDir
while ($true) {
    $portInUse = netstat -ano | Select-String -SimpleMatch ":8080" | Select-String -SimpleMatch "LISTENING"
    if ($portInUse) {
        Write-Host "[$(Get-Date -Format s)] Port 8080 is already in use; leaving the existing backend untouched."
        break
    }

    Write-Host "[$(Get-Date -Format s)] Starting TripNest backend on http://localhost:8080"
    & .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
    $exitCode = $LASTEXITCODE
    Write-Warning "TripNest backend exited with code $exitCode. Restarting in 5 seconds..."
    Start-Sleep -Seconds 5
}
