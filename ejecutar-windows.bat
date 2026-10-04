@echo off
setlocal
cd /d "%~dp0"
set "JAR_FILE=target\gestor-contrasenas-1.0.0.jar"

if not exist "%JAR_FILE%" (
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo No se encontro Maven. Instala Maven o genera el JAR con mvn package.
        exit /b 1
    )
    call mvn -q -DskipTests package
    if errorlevel 1 exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo No se encontro Java. Instala Java 8 o posterior.
    exit /b 1
)

java -jar "%JAR_FILE%" %*