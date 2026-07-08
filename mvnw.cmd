@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Begin all REM://REMs
@REM Maven Wrapper script for Windows

@echo off
@setlocal

set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@REM Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto error

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto error

:execute
@REM Setup the command line
set WRAPPER_JAR="%~dp0\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES="%~dp0\.mvn\wrapper\maven-wrapper.properties"

@REM Check if wrapper jar exists; if not, download it
if exist %WRAPPER_JAR% goto launchWrapper

echo Downloading Maven Wrapper...
@REM Download the wrapper jar using PowerShell
powershell -Command "&{$props = Get-Content %WRAPPER_PROPERTIES%; foreach($line in $props) { if($line -match 'wrapperUrl=(.*)') { $url=$matches[1]; Invoke-WebRequest -Uri $url -OutFile %WRAPPER_JAR% -UseBasicParsing; break }}}"

if exist %WRAPPER_JAR% goto launchWrapper

@REM Fallback: try downloading Maven directly and use it
echo Maven Wrapper jar not available. Downloading Maven directly...
for /f "tokens=2 delims==" %%a in ('findstr "distributionUrl" %WRAPPER_PROPERTIES%') do set DOWNLOAD_URL=%%a

set MAVEN_ZIP="%TEMP%\maven-dist.zip"
set MAVEN_DIR="%USERPROFILE%\.m2\wrapper\dists\maven"

if exist "%MAVEN_DIR%\bin\mvn.cmd" goto runMavenDirect

powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile %MAVEN_ZIP% -UseBasicParsing; Expand-Archive -Path %MAVEN_ZIP% -DestinationPath '%MAVEN_DIR%' -Force"

@REM Find the extracted maven directory
for /d %%i in ("%MAVEN_DIR%\apache-maven-*") do set MAVEN_EXTRACTED=%%i

:runMavenDirect
for /d %%i in ("%MAVEN_DIR%\apache-maven-*") do set MAVEN_EXTRACTED=%%i
"%MAVEN_EXTRACTED%\bin\mvn.cmd" %*
if ERRORLEVEL 1 goto error
goto end

:launchWrapper
"%JAVA_EXE%" %MAVEN_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
