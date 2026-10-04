$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error "No se encontró Maven. Instala Maven y vuelve a intentarlo."
    exit 1
}
if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    Write-Error "No se encontró jpackage. Añade al PATH el bin de un JDK 16 o posterior."
    exit 1
}

& mvn clean package
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$staging = "target\portable-input"
$output = "portable\KeyVault"
New-Item -ItemType Directory -Force -Path $staging, "portable" | Out-Null
Copy-Item "target\gestor-contrasenas-1.0.0.jar" $staging
if (Test-Path $output) {
    Remove-Item -Recurse -Force $output
}

$jpackageArgs = @(
    "--type", "app-image",
    "--name", "KeyVault",
    "--app-version", "1.0.0",
    "--input", $staging,
    "--main-jar", "gestor-contrasenas-1.0.0.jar",
    "--main-class", "gestorcontrasenas.GestorContrasenas",
    "--dest", "portable"
)
& jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$launcher = @"
@echo off
setlocal
cd /d "%~dp0"
start "" /wait "%~dp0KeyVault.exe"
exit /b %errorlevel%
"@
Set-Content -Path (Join-Path $output "KeyVault.bat") -Value $launcher -Encoding ASCII
Write-Host "Distribución portable creada en $output"