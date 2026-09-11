# KisanProcure Intelligent Launcher v2
# Double-click START_KISAN_SYSTEM.bat to run this
Set-StrictMode -Off
$ROOT = Split-Path -Parent $PSScriptRoot
$SCRIPTS = $PSScriptRoot
$BP = 5000
$AP = 3000
$CF = Join-Path $SCRIPTS 'cloudflared.exe'
$RUNTIME = Join-Path $ROOT 'runtime'
$CFLOG = Join-Path $RUNTIME 'cloudflare.log'
$URLFILE = Join-Path $RUNTIME 'tunnel-url.txt'
$STATUSFILE = Join-Path $RUNTIME 'tunnel-status.json'
$REPORT = Join-Path $RUNTIME 'connection-report.txt'

if (-not (Test-Path $RUNTIME)) { New-Item -ItemType Directory $RUNTIME | Out-Null }
if (Test-Path $URLFILE) { Remove-Item $URLFILE -Force }
if (Test-Path $CFLOG)   { Remove-Item $CFLOG -Force }

function Write-Ok($msg, $ok, $d='') {
    $color = if ($ok) { 'Green' } else { 'Red' }
    $icon  = if ($ok) { '[OK]  ' } else { '[FAIL]' }
    Write-Host ($icon + ' ' + $msg + ' ' + $d) -ForegroundColor $color
}

function Port-Up($p) {
    $r = netstat -ano 2>$null | Select-String (':'+$p+' ')
    return $r -ne $null
}

function Wait-Port($p, $t=15) {
    for ($i=0; $i -lt $t; $i++) {
        if (Port-Up $p) { return $true }
        Start-Sleep 1
    }
    return $false
}

function Hget($url) {
    try {
        $r = Invoke-WebRequest -Uri $url -TimeoutSec 8 -UseBasicParsing -ErrorAction Stop
        return $r.StatusCode -eq 200
    } catch { return $false }
}

function Jget($url) {
    try {
        $r = Invoke-WebRequest -Uri $url -TimeoutSec 10 -UseBasicParsing -ErrorAction Stop
        return $r.Content | ConvertFrom-Json
    } catch { return $null }
}

Clear-Host
Write-Host '================================================' -ForegroundColor Cyan
Write-Host '   KISANPROCURE SYSTEM LAUNCHER' -ForegroundColor Cyan
Write-Host '================================================' -ForegroundColor Cyan
Write-Host ''

# ---- [1] MySQL ----
Write-Host '[1/6] MySQL Database...' -ForegroundColor Yellow
if (Port-Up 3306) {
    Write-Ok 'MySQL' $true 'already running on port 3306'
} elseif (Test-Path 'C:\xampp\mysql\bin\mysqld.exe') {
    Start-Process 'C:\xampp\mysql\bin\mysqld.exe' '--defaults-file=C:\xampp\mysql\bin\my.ini --standalone' -WindowStyle Hidden
    Start-Sleep 5
    Write-Ok 'MySQL' (Port-Up 3306) 'port 3306'
} else {
    Write-Ok 'MySQL' $false 'XAMPP not found'
}

# ---- [2] Backend ----
Write-Host ''; Write-Host '[2/6] Backend :5000...' -ForegroundColor Yellow
if (Port-Up $BP) {
    $oldpid = ((netstat -ano 2>$null | Select-String (':5000 .*LISTENING')) -split '\s+')[-1]
    if ($oldpid -match '^\d+$') { Stop-Process -Id ([int]$oldpid) -Force -ErrorAction SilentlyContinue }
    Start-Sleep 2
}
$bdir = Join-Path $ROOT 'backend'
$np = Start-Process 'node' 'dist\server.js' -WorkingDirectory $bdir -WindowStyle Minimized -PassThru
$bok = Wait-Port $BP 15
if ($bok) {
    $h = Jget 'http://127.0.0.1:5000/health'
    Write-Ok 'Backend' $true ('status=' + $h.status)
} else {
    Write-Ok 'Backend' $false 'did not start within 15s'
}

# ---- [3] Admin ----
Write-Host ''; Write-Host '[3/6] Admin Panel :3000...' -ForegroundColor Yellow
if (Port-Up $AP) {
    Write-Ok 'Admin' $true 'already running'
} else {
    $adir = Join-Path $ROOT 'admin'
    if (Test-Path (Join-Path $adir 'dist')) {
        Start-Process 'cmd' '/c npx serve dist -p 3000' -WorkingDirectory $adir -WindowStyle Minimized
    } else {
        Start-Process 'cmd' '/c npm run dev' -WorkingDirectory $adir -WindowStyle Minimized
    }
    Start-Sleep 3
    Write-Ok 'Admin' (Port-Up $AP) 'http://localhost:3000'
}

# ---- [4] ADB Reverse ----
Write-Host ''; Write-Host '[4/6] ADB USB Reverse (local dev)...' -ForegroundColor Yellow
$adb = ($env:LOCALAPPDATA + '\Android\Sdk\platform-tools\adb.exe')
if (Test-Path $adb) {
    & $adb reverse tcp:5000 tcp:5000 2>$null
    Write-Ok 'ADB reverse' $true 'USB -> localhost:5000'
} else {
    Write-Host '  [SKIP] ADB not found' -ForegroundColor Gray
}

# ---- [5] Cloudflare Tunnel ----
Write-Host ''; Write-Host '[5/6] Cloudflare Tunnel...' -ForegroundColor Yellow
$turl = $null
$tOk  = $false
if ((Test-Path $CF) -and $bok) {
    $cfp = Start-Process $CF 'tunnel --url http://127.0.0.1:5000' `
        -RedirectStandardOutput $CFLOG -RedirectStandardError $CFLOG `
        -WindowStyle Hidden -PassThru
    Write-Host '  Waiting for URL (up to 30s)...' -ForegroundColor Gray
    for ($i=0; $i -lt 30; $i++) {
        Start-Sleep 1
        if (Test-Path $CFLOG) {
            $txt = Get-Content $CFLOG -Raw -ErrorAction SilentlyContinue
            if ($txt -match 'https://([a-z0-9\-]+\.trycloudflare\.com)') {
                $turl = 'https://' + $Matches[1]
                break
            }
        }
    }
    if ($turl) {
        Set-Content $URLFILE $turl -Encoding UTF8
        $statusObj = @{
            running     = $true
            url         = $turl
            localTarget = 'http://127.0.0.1:5000'
            startedAt   = (Get-Date -Format 'o')
            pid         = $cfp.Id
        }
        $statusObj | ConvertTo-Json | Set-Content $STATUSFILE -Encoding UTF8
        Write-Ok 'Tunnel URL detected' $true $turl

        # Automatically sync detected tunnel URL with Android AppConfig
        $appConfigPath = Join-Path $ROOT 'android\app\src\main\java\com\kisanprocure\app\utils\AppConfig.kt'
        if (Test-Path $appConfigPath) {
            $cfg = Get-Content $appConfigPath -Raw
            $cfg = $cfg -replace 'const val LIVE_CLOUDFLARE_URL = ".*?"', ('const val LIVE_CLOUDFLARE_URL = "' + $turl + '"')
            Set-Content $appConfigPath $cfg -Encoding UTF8
            Write-Ok 'Android AppConfig' $true ('Synced live URL -> ' + $turl)
        }
        Write-Host '  Testing public HTTPS /health (12s propagation)...' -ForegroundColor Gray
        Start-Sleep 12
        $tOk = Hget ($turl + '/health')
        if (-not $tOk) {
            Start-Sleep 8
            $tOk = Hget ($turl + '/health')
        }
        Write-Ok 'Public /health reachable' $tOk ($turl + '/health')
    } else {
        Write-Ok 'Tunnel URL' $false 'not detected - check runtime\cloudflare.log'
    }
} else {
    Write-Ok 'Cloudflare' $false 'skipped (cloudflared not found or backend down)'
}

# ---- [6] Socket.IO ----
Write-Host ''; Write-Host '[6/6] Socket.IO...' -ForegroundColor Yellow
$sh  = Jget 'http://127.0.0.1:5000/health/socket'
$sok = ($sh -ne $null) -and ($sh.success -eq $true)
Write-Ok 'Socket.IO' $sok

# ---- Final Status ----
Write-Host ''
Write-Host '================================================' -ForegroundColor Cyan
Write-Host '              FINAL STATUS' -ForegroundColor Cyan
Write-Host '================================================' -ForegroundColor Cyan
Write-Ok 'MySQL        :3306' (Port-Up 3306)
Write-Ok 'Backend      :5000' $bok
Write-Ok 'Admin        :3000' (Port-Up $AP)
Write-Ok 'Socket.IO' $sok
Write-Ok 'Cloudflare Tunnel' ($null -ne $turl)
Write-Ok 'Public Health' $tOk
Write-Host ''
Write-Host 'LOCAL:  http://127.0.0.1:5000' -ForegroundColor White

if ($turl) {
    Write-Host 'PUBLIC: ' -NoNewline -ForegroundColor Cyan
    Write-Host $turl -ForegroundColor Green
    Write-Host 'CONFIG: ' -NoNewline -ForegroundColor Cyan
    Write-Host ($turl + '/api/system/public-config') -ForegroundColor White
    Write-Host ''
    Write-Host '================================================' -ForegroundColor Green
    Write-Host '   SYSTEM ONLINE - USE PUBLIC URL IN ANDROID' -ForegroundColor Green
    Write-Host '================================================' -ForegroundColor Green
} else {
    Write-Host ''
    Write-Host '================================================' -ForegroundColor Yellow
    Write-Host '   LOCAL ONLY - CLOUDFLARE TUNNEL NOT ACTIVE' -ForegroundColor Yellow
    Write-Host '================================================' -ForegroundColor Yellow
}

# ---- Write Report ----
$tunnelStatus = if ($null -ne $turl) { 'RUNNING: ' + $turl } else { 'NOT RUNNING' }
$mysqlStatus  = if (Port-Up 3306)    { 'PASS' } else { 'FAIL' }
$backStatus   = if ($bok)            { 'PASS' } else { 'FAIL' }
$adminStatus  = if (Port-Up $AP)     { 'PASS' } else { 'FAIL' }
$socketStatus = if ($sok)            { 'PASS' } else { 'FAIL' }
$publicStatus = if ($tOk)            { 'PASS' } else { 'FAIL' }
$publicUrl    = if ($turl)           { $turl }  else { 'N/A' }

$lines = @(
    'KISANPROCURE CONNECTION REPORT'
    '================================'
    ('Generated : ' + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'))
    ('MySQL     : ' + $mysqlStatus)
    ('Backend   : ' + $backStatus)
    ('Admin     : ' + $adminStatus)
    ('Socket.IO : ' + $socketStatus)
    ('Tunnel    : ' + $tunnelStatus)
    ('PublicH   : ' + $publicStatus)
    ('Local URL : http://127.0.0.1:5000')
    ('Public URL: ' + $publicUrl)
)
$lines | Set-Content $REPORT -Encoding UTF8
Write-Host ''
Write-Host 'Report saved: runtime\connection-report.txt' -ForegroundColor Gray
Write-Host 'Press Ctrl+C to stop services.' -ForegroundColor Gray
if ($null -ne $np) { Wait-Process -Id $np.Id -ErrorAction SilentlyContinue }
