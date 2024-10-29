@echo off
setlocal

docker --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Docker is already installed.
) else (
    echo Docker not found. Installing Docker Desktop...

    dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
    dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
    dism.exe /Online /Enable-Feature /All /FeatureName:Microsoft-Hyper-V

    if %ERRORLEVEL% EQU 3010 (
        echo Restart is required. Please restart your computer and re-run this script.
        exit /b 1
    )

    wsl --list --verbose >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        curl -L -o "wsl_update_x64.msi" "https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi"
        if exist "wsl_update_x64.msi" (
            start /wait msiexec /i wsl_update_x64.msi /quiet
            del wsl_update_x64.msi
	    wsl --set-default-version 2 >nul 2>&1
        ) else (
            echo Failed to download WSL 2 kernel update. Exiting.
            exit /b 1
        )
    )

	curl -L -o "DockerDesktopInstaller.exe" "https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe"
	if exist "DockerDesktopInstaller.exe" (
	    start /wait "" "DockerDesktopInstaller.exe" install --quiet
	) else (
	    echo Failed to find the installer. Exiting.
	    exit /b 1
	)
)

docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Docker installation failed. Exiting.
    exit /b 1
)

if exist "docker-compose.yml" (
    echo Running Docker Compose...
    docker compose up -d --build
) else (
    echo docker-compose.yml not found. Exiting.
    exit /b 1
)

endlocal
