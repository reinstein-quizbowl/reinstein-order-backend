#!/bin/bash

pid=`ps -ef | grep $REINSTEIN_APP_NAME | grep $JAVA_HOME/bin/java | awk '{print $2}'`

if [ -z "${pid}" ]; then
    echo "$REINSTEIN_APP_NAME is not running"
else
    echo "Killing process $pid"
    kill -9 $pid
    echo "Shut down $REINSTEIN_APP_NAME"
fi
