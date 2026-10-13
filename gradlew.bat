@rem Gradle wrapper toi gian cho Windows (LUNA36 chua cai Gradle).
@echo off
where gradle >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
  echo Chua co Gradle. Build tren GitHub Actions (xem README.md).
  exit /b 1
)
gradle %*
