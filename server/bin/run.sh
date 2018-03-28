#!/bin/sh

# DO NOT ALTER OR @REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
# Copyright 2009 - 2014 Luca Mingardi.
# 
# This file is part of jeeObserver.

export JOS_SERVER_PORT=5688
export JOS_DATABASE_HANDLER=jeeobserver.server.FileDatabaseHandler
export JOS_DATABASE_FILE_PATH=./JEEOBSERVER_4_2
export JOS_LOGGER_LEVEL=INFO
export JOS_SAMPLING_PERIOD=MINUTE
export JOS_STORAGE_PERIOD=90 DAY

export JOS_NOTIFICATION_EMAIL_SMTP_HOST=
export JOS_NOTIFICATION_EMAIL_SMTP_PORT=
export JOS_NOTIFICATION_EMAIL_SMTP_USER=
export JOS_NOTIFICATION_EMAIL_SMTP_PASSWORD=

export JOS_NOTIFICATION_EMAIL_FROM_ADDRESS=
export JOS_NOTIFICATION_EMAIL_FROM_NAME=

export JOS_NOTIFICATION_EMAIL_CONTENT_TYPE=text/plain


if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

JAVA_OPT=-Xmx128m
#Add additional libs to classpath after ../jeeobserver.jar ';' sepratated
jeeobserver_exec_command="exec \"$JAVACMD\" $JAVA_OPT -classpath ../../jeeobserver.jar jeeobserver.JeeObserverServerContext"
eval $jeeobserver_exec_command
