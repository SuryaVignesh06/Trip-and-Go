$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$backendDir = Join-Path $projectRoot "tripnest-backend"
$frontendDir = Join-Path $projectRoot "frontend"
$logDir = Join-Path $projectRoot ".local-logs"
$backendRunner = Join-Path $projectRoot "run-backend.ps1"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

function Test-PortListening([int]$Port) {
    # netstat remains reliable when this script is run from an environment that
    # cannot inspect other users' socket objects through Get-NetTCPConnection.
    return [bool](netstat -ano | Select-String -Pattern "^\s*TCP\s+.*:$Port\s+.*LISTENING\s+\d+$")
}

if (-not (Test-PortListening 8080)) {
    Start-Process -FilePath "powershell.exe" -WorkingDirectory $projectRoot -WindowStyle Hidden `
        -ArgumentList "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $backendRunner `
        -RedirectStandardOutput (Join-Path $logDir "backend.log") `
        -RedirectStandardError (Join-Path $logDir "backend-error.log")
}

if (-not (Test-PortListening 3000)) {
    Start-Process -FilePath "cmd.exe" -WorkingDirectory $frontendDir -WindowStyle Hidden `
        -ArgumentList "/c", "npm.cmd run dev" `
        -RedirectStandardOutput (Join-Path $logDir "frontend.log") `
        -RedirectStandardError (Join-Path $logDir "frontend-error.log")
}

Write-Host "TripNest is starting."
Write-Host "Frontend: http://localhost:3000"
Write-Host "Backend:  http://localhost:8080"
Write-Host "Logs:     $logDir"
