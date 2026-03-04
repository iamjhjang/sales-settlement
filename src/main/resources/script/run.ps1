param(
  [string]$DbService = "postgres",   # docker-compose.yml 서비스명
  [int]$DockerTimeoutSec = 240,
  [int]$DbTimeoutSec = 180,
  [switch]$ResetDb                   # DB 볼륨까지 초기화: -ResetDb
)

$ErrorActionPreference = "Stop"

# (선택) 한글/출력 깨짐 방지
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

function Find-ProjectRoot([string]$startDir) {
  $dir = (Resolve-Path $startDir).Path
  while ($true) {
    $hasGradleWrapper = (Test-Path (Join-Path $dir "gradlew.bat")) -or (Test-Path (Join-Path $dir "gradlew"))
    $hasCompose = (Test-Path (Join-Path $dir "docker-compose.yml")) -or (Test-Path (Join-Path $dir "compose.yml"))
    if ($hasGradleWrapper -and $hasCompose) { return $dir }

    $parent = Split-Path $dir -Parent
    if ([string]::IsNullOrEmpty($parent) -or $parent -eq $dir) { break }
    $dir = $parent
  }
  throw "프로젝트 루트를 찾지 못했습니다. (gradlew(.bat) + docker-compose.yml 기준)"
}

function Start-DockerDesktopIfNeeded {
  $dockerDesktopExe = "$Env:ProgramFiles\Docker\Docker\Docker Desktop.exe"
  if (Test-Path $dockerDesktopExe) {
    $p = Get-Process -Name "Docker Desktop" -ErrorAction SilentlyContinue
    if (-not $p) { Start-Process $dockerDesktopExe | Out-Null }
  }

  try {
    $svc = Get-Service -Name "com.docker.service" -ErrorAction SilentlyContinue
    if ($svc -and $svc.Status -ne "Running") { Start-Service "com.docker.service" }
  } catch {}
}

function Wait-Docker([int]$timeoutSec) {
  $sw = [Diagnostics.Stopwatch]::StartNew()
  while ($true) {
    try {
      docker version *> $null  # Server까지 연결되면 성공
      return
    } catch {
      if ($sw.Elapsed.TotalSeconds -ge $timeoutSec) {
        throw @"
Docker 엔진 연결 실패.
- Docker Desktop 실행 여부 확인
- Linux containers(WSL2) 모드 확인
- 서비스(com.docker.service) 실행 여부 확인
"@
      }
      Start-Sleep -Seconds 2
    }
  }
}

function Ensure-DockerContext {
  try {
    $ctx = (docker context show).Trim()
    if ($ctx -ne "desktop-linux") {
      docker context use desktop-linux | Out-Null
    }
  } catch {}
}

function Wait-ComposeServiceReady([string]$serviceName, [int]$timeoutSec) {
  $sw = [Diagnostics.Stopwatch]::StartNew()
  while ($true) {
    $cid = (docker compose ps -q $serviceName) 2>$null
    if ($cid) {
      $health = (docker inspect -f "{{if .State.Health}}{{.State.Health.Status}}{{end}}" $cid) 2>$null
      if ($health) {
        if ($health -eq "healthy") { return }
      } else {
        $status = (docker inspect -f "{{.State.Status}}" $cid) 2>$null
        if ($status -eq "running") { return }
      }
    }

    if ($sw.Elapsed.TotalSeconds -ge $timeoutSec) {
      docker compose ps | Out-Host
      docker compose logs $serviceName --tail 120 | Out-Host
      throw "$serviceName 서비스가 ready 상태가 되지 않았습니다."
    }
    Start-Sleep -Seconds 2
  }
}

# ===== 실행 시작 =====
$projectRoot = Find-ProjectRoot $PSScriptRoot
Set-Location $projectRoot

Write-Host ""
Write-Host "ProjectRoot: $projectRoot"
Write-Host ""

Start-DockerDesktopIfNeeded
Wait-Docker $DockerTimeoutSec
Ensure-DockerContext

if ($ResetDb) {
  docker compose down -v
}

docker compose pull
docker compose up -d --remove-orphans
docker compose ps

Wait-ComposeServiceReady $DbService $DbTimeoutSec

Write-Host ""
Write-Host "Docker/DB 기동 완료."
Write-Host ""

$gradleBat = Join-Path $projectRoot "gradlew.bat"
$gradleSh  = Join-Path $projectRoot "gradlew"

if (Test-Path $gradleBat) {
  & $gradleBat clean bootRun
} elseif (Test-Path $gradleSh) {
  & $gradleSh clean bootRun
} else {
  throw "gradlew.bat/gradlew를 찾지 못했습니다."
}