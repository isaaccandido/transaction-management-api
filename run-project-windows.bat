@echo off
setlocal

set JAVA_VERSION=21
set JAVA_URL=https://download.oracle.com/java/21/latest/jdk-21_windows-x64_bin.zip
set JAVA_DIR=%~dp0java
set TEMP_JAVA_DIR=%JAVA_DIR%\jdk-temp

for /d %%D in ("%JAVA_DIR%\jdk-*") do (
    set "EXISTING_JAVA=%%D"
)

if not defined EXISTING_JAVA (
    echo Standalone Java not found. Downloading JDK %JAVA_VERSION%...

    mkdir "%TEMP_JAVA_DIR%"

    curl -L -o "%TEMP_JAVA_DIR%\jdk.zip" %JAVA_URL%

    echo Extracting JDK...
    powershell -command "Expand-Archive -Path '%TEMP_JAVA_DIR%\jdk.zip' -DestinationPath '%TEMP_JAVA_DIR%'"

    for /d %%D in ("%TEMP_JAVA_DIR%\jdk-*") do (
        move "%%D" "%JAVA_DIR%\jdk-%JAVA_VERSION%"
        set "EXISTING_JAVA=%JAVA_DIR%\jdk-%JAVA_VERSION%"
    )

    del "%TEMP_JAVA_DIR%\jdk.zip"
    rmdir /s /q "%TEMP_JAVA_DIR%"
)

set JAVA_HOME=%EXISTING_JAVA%
set PATH=%JAVA_HOME%\bin;%PATH%

java -version
if %ERRORLEVEL% neq 0 (
    echo Failed to set up Java. Exiting...
    exit /b 1
)

echo Running Spring Boot application...
mvnw clean package spring-boot:run

endlocal
