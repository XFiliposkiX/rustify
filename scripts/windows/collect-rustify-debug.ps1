param(
    [string]$InstancePath = (Join-Path $env:APPDATA ".minecraft")
)

$ErrorActionPreference = "Stop"
$instance = [System.IO.Path]::GetFullPath($InstancePath)
if (-not (Test-Path -LiteralPath $instance -PathType Container)) {
    throw "Minecraft instance folder does not exist: $instance"
}

$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$work = Join-Path $env:TEMP "rustify-debug-$stamp"
$output = Join-Path ([Environment]::GetFolderPath("Desktop")) "rustify-debug-$stamp.zip"
New-Item -ItemType Directory -Path $work | Out-Null

function Copy-DiagnosticFile([string]$RelativePath) {
    $source = Join-Path $instance $RelativePath
    if (Test-Path -LiteralPath $source -PathType Leaf) {
        $target = Join-Path $work $RelativePath
        New-Item -ItemType Directory -Path (Split-Path $target) -Force | Out-Null
        Copy-Item -LiteralPath $source -Destination $target
    }
}

Copy-DiagnosticFile "logs\latest.log"
Copy-DiagnosticFile "rustify\benchmarks\latest.txt"
Copy-DiagnosticFile "rustify\benchmarks\latest.json"
Copy-DiagnosticFile "config\rustify.properties"

$crashDirectory = Join-Path $instance "crash-reports"
if (Test-Path -LiteralPath $crashDirectory -PathType Container) {
    $latestCrash = Get-ChildItem -LiteralPath $crashDirectory -Filter "crash-*.txt" -File |
        Sort-Object LastWriteTimeUtc -Descending | Select-Object -First 1
    if ($null -ne $latestCrash) {
        $targetDirectory = Join-Path $work "crash-reports"
        New-Item -ItemType Directory -Path $targetDirectory | Out-Null
        Copy-Item -LiteralPath $latestCrash.FullName -Destination $targetDirectory
    }
}

$javaLines = @()
try {
    $javaLines = & java -version 2>&1 | ForEach-Object { $_.ToString() }
} catch {
    $javaLines = @("java command unavailable")
}

$system = [ordered]@{
    collectedAtUtc = (Get-Date).ToUniversalTime().ToString("o")
    instancePath = $instance
    windows = [System.Environment]::OSVersion.VersionString
    architecture = $env:PROCESSOR_ARCHITECTURE
    logicalProcessors = [System.Environment]::ProcessorCount
    java = $javaLines
    collectorVersion = 1
}
$system | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $work "system.json") -Encoding UTF8

$manifest = Get-ChildItem -LiteralPath $work -File -Recurse | ForEach-Object {
    [ordered]@{
        path = $_.FullName.Substring($work.Length + 1)
        bytes = $_.Length
        sha256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    }
}
$manifest | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $work "manifest.json") -Encoding UTF8

if (Test-Path -LiteralPath $output) {
    Remove-Item -LiteralPath $output
}
Compress-Archive -Path (Join-Path $work "*") -DestinationPath $output
Remove-Item -LiteralPath $work -Recurse
Write-Host "Rustify diagnostics created: $output"
Write-Host "Send that ZIP file back for analysis."
