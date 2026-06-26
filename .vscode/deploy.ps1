$devices = adb devices | Select-String -Pattern '^\S+\s+device$'
if ($devices.Count -eq 0) {
    Write-Host ""
    Write-Host "  *** NO CONTROL HUB CONNECTED ***" -ForegroundColor Red
    Write-Host "  Connect via USB or run 'Connect to Control Hub over WiFi' first." -ForegroundColor Yellow
    Write-Host ""
    exit 1
}
Write-Host "Control Hub found: $devices" -ForegroundColor Green
.\gradlew.bat :TeamCode:installDebug 2>&1 | Where-Object {
    $_ -notmatch 'uses or overrides a deprecated API' -and
    $_ -notmatch 'Recompile with -Xlint:deprecation' -and
    $_ -notmatch '^> Task :' -and
    $_ -notmatch '^\d+ actionable tasks:' -and
    $_ -match '\S'
}
if ($LASTEXITCODE -eq 0) {
    Write-Host "The code was sent to the robot. It will reboot now. You can unplug the cable while it reboots." -ForegroundColor Green
}
