@echo off
setlocal

REM Check if Docker is installed
docker --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Docker is already installed.
) else (
    echo Docker not found. Installing Docker Desktop...

    REM Enable WSL, Virtual Machine Platform, and Hyper-V
    dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
    dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
    dism.exe /Online /Enable-Feature /All /FeatureName:Microsoft-Hyper-V

    REM Check if reboot is needed
    if %ERRORLEVEL% EQU 3010 (
        echo Restart is required. Please restart your computer and re-run this script.
        exit /b 1
    )

    REM Install WSL if it's not already installed
    wsl --list --verbose >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo WSL not found. Installing WSL...
        curl -L -o "wsl_update_x64.msi" "https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi"
        if exist "wsl_update_x64.msi" (
            echo Installing WSL kernel update...
            start /wait msiexec /i wsl_update_x64.msi /quiet /norestart
            del wsl_update_x64.msi
        ) else (
            echo Failed to download WSL 2 kernel update. Exiting.
            exit /b 1
        )
    )

    REM Set WSL 2 as the default version and handle errors
    wsl --set-default-version 2 >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo WSL 2 kernel update is required. Downloading installer...
        curl -L -o "wsl_update_x64.msi" "https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi"
        if exist "wsl_update_x64.msi" (
            echo Installing WSL 2 kernel update...
            start /wait msiexec /i wsl_update_x64.msi /quiet /norestart
            del wsl_update_x64.msi
        ) else (
            echo Failed to download WSL 2 kernel update. Exiting.
            exit /b 1
        )
        
        REM Retry setting WSL 2 as default
        wsl --set-default-version 2 >nul 2>&1
        if %ERRORLEVEL% NEQ 0 (
            echo Failed to set WSL 2 as the default version. Exiting.
            exit /b 1
        )
    )

    REM Download and install Docker Desktop
    set "docker_installer=%TEMP%\DockerDesktopInstaller.exe"
    echo Downloading Docker Desktop...
    echo Docker installer path: %docker_installer%

    REM Use curl to download Docker Desktop installer
    curl -L -o "%docker_installer%" "https://desktop.docker.com/win/stable/Docker%20Desktop%20Installer.exe"
    
    if exist "%docker_installer%" (
        echo Installing Docker Desktop...
        start /wait "" "%docker_installer%" --quiet
        del "%docker_installer%"
    ) else (
        echo Failed to download Docker Desktop. Exiting.
        exit /b 1
    )
)

REM Verify Docker installation
docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Docker installation failed. Exiting.
    exit /b 1
)

REM Run Docker Compose if docker-compose.yml exists
if exist "docker-compose.yml" (
    echo Running Docker Compose...
    docker compose up -d --build
) else (
    echo docker-compose.yml not found. Exiting.
    exit /b 1
)

endlocal
