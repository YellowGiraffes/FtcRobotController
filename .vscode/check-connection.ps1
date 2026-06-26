$output = adb devices 2>&1
$isConnected = ($output | Where-Object { $_ -match '\bdevice$' }).Count -gt 0

if ($isConnected) {
    Write-Host 'The robot is connected.' -ForegroundColor Green
    $newLabel = '$(check) The robot is connected'
} else {
    Write-Host 'The robot is not connected.' -ForegroundColor Red
    $newLabel = '$(error) The robot is not connected'
}

# Update the status bar button label so the result is visible without opening the terminal
$settingsPath = Join-Path $PSScriptRoot 'settings.json'
$content = Get-Content $settingsPath -Raw
$content = $content -replace '"\$\((?:plug|check|error)\) (?:Check connection|The robot is (?:not )?connected)"', ('"' + $newLabel + '"')
[System.IO.File]::WriteAllText($settingsPath, $content, [System.Text.Encoding]::UTF8)
