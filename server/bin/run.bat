@echo off

@REM DO NOT ALTER OR @REMOVE COPYRIGHT NOTICES OR THIS HEADER.
@REM 
@REM Copyright 2009 - 2014 Luca Mingardi.
@REM 
@REM This file is part of jeeObserver.

:setEnvironmentVariables
set JOS_SERVER_PORT=5688
set JOS_DATABASE_HANDLER=jeeobserver.server.FileDatabaseHandler
set JOS_LOGGER_LEVEL=INFO
set JOS_SAMPLING_PERIOD=MINUTE
set JOS_STORAGE_PERIOD=90 DAY

set JOS_DATABASE_FILE_PATH=./JEEOBSERVER_4_2

set JOS_NOTIFICATION_EMAIL_SMTP_HOST=
set JOS_NOTIFICATION_EMAIL_SMTP_PORT=
set JOS_NOTIFICATION_EMAIL_SMTP_USER=
set JOS_NOTIFICATION_EMAIL_SMTP_PASSWORD=

set JOS_NOTIFICATION_EMAIL_FROM_ADDRESS=
set JOS_NOTIFICATION_EMAIL_FROM_NAME=

set JOS_NOTIFICATION_EMAIL_CONTENT_TYPE=text/plain

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto runServer

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:runServer
set JAVA_OPT=-Xmx128m
set JOS_CLASSPATH="../../jeeobserver.jar;"
"%_JAVACMD%" %JAVA_OPT% -classpath %JOS_CLASSPATH% jeeobserver.server.JeeObserverServerContext