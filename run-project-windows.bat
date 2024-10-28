@echo off
setlocal

set DOCKER_CMD=docker
set TEMP_DOCKER_SCRIPT=get-docker.bat
set DOCKER_URL=https://get.docker.com
set JAVA_DIR=%~dp0java

where %DOCKER_CMD% >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Docker is not installed. Installing Docker...

    curl -fsSL %DOCKER_URL% -o %TEMP_DOCKER_SCRIPT%
    call %TEMP_DOCKER_SCRIPT%
    del %TEMP_DOCKER_SCRIPT%

    echo Docker installed successfully.
) else (
    echo Docker is already installed.
)

docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Starting Docker service...
    net start com.docker.service >nul 2>&1
)

net localgroup docker %USERNAME% >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Adding current user to the 'docker' group...
    net localgroup docker %USERNAME% /add >nul 2>&1
    echo You may need to log out and log back in for group changes to take effect.
)

echo Running Spring Boot application...
mvnw clean package spring-boot:run

endlocal
